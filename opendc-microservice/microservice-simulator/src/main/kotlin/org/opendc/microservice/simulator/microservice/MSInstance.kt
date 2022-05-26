package org.opendc.microservice.simulator.microservice

import io.opentelemetry.api.metrics.LongHistogram
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.opendc.microservice.simulator.state.RegistryManager
import org.opendc.microservice.simulator.workload.MSWorkloadMapper
import org.opendc.simulator.compute.SimBareMetalMachine
import org.opendc.simulator.compute.SimMachine
import org.opendc.simulator.compute.SimMachineContext
import org.opendc.simulator.compute.model.MachineModel
import org.opendc.simulator.compute.power.ConstantPowerModel
import org.opendc.simulator.compute.power.SimplePowerDriver
import org.opendc.simulator.compute.workload.SimWorkload
import org.opendc.simulator.flow.FlowEngine
import java.time.Clock
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
* Microservice instance has an id.
* The clock, scope, model are used to config the machine to run the instance on.
 */
public class MSInstance(private val msId: UUID,
                        private val id: UUID,
                        private val clock: Clock,
                        private val scope: CoroutineScope,
                        private val model: MachineModel,
                        private val registryManager: RegistryManager,
                        private val mapper: MSWorkloadMapper
                                  ){

    private val workload = mapper.createWorkload(this)

    private val queue = ArrayDeque<InvocationRequest>()

    /**
     * A channel used to signal that new invocations have been enqueued.
     */
    private val chan = Channel<Unit>(Channel.RENDEZVOUS)

    /**
     * The machine that will execute the workloads.
     */
    private val machine: SimMachine = SimBareMetalMachine(
        FlowEngine(scope.coroutineContext, clock),
        model,
        SimplePowerDriver(ConstantPowerModel(0.0))
    )


    /**
     * The job associated with the lifecycle of the instance.
     */
    private var job: Job? = null

    private var runningLoadEnd: Long = 0

    private var state: InstanceState = InstanceState.Idle

    init{

        registryManager.registerInstance(this)

    }


    public fun getId(): UUID {

        return id

    }


    public fun getMSId(): UUID {

        return msId

    }

    /**
     * load is exe time.
     * includes left over currently running load time
     */
    public fun load(): Int {

        var load = 0

        val currentTime = clock.millis()

        if(runningLoadEnd > currentTime) load = (runningLoadEnd - currentTime).toInt()

        for(request in queue){

            load += request.exeTime.toInt()

        }

        println("Queued load for instance ${getId()} is $load")

        return load

    }


    /**
     * connections are nr of requests queued
     */
    public fun connections(): Int {

        var connections = 0

        if(state == InstanceState.Active) connections = queue.size + 1

        else connections = queue.size

        println("Connections for instance ${getId()} is $connections")

        return connections

    }


    public fun run(){

        job = scope.launch {

            launch{

                // println(" ${clock.millis()} launching instance with UUID "+getId()+" at coroutine ${Thread.currentThread().name} ")

                machine.startWorkload(workload)

                // println(" ${clock.millis()} Finished instance workload"+" at coroutine ${Thread.currentThread().name}" )


            }

            while (isActive) {
                if (queue.isEmpty()) {
                    chan.receive()
                }


                while (queue.isNotEmpty()) {

                    state = InstanceState.Active

                    println(" ${clock.millis()} Starting queued request at coroutine ${Thread.currentThread().name} on instance ${getId()}")

                    val request = queue.poll()

                    runningLoadEnd = clock.millis() + request.exeTime

                    println("exeTime of this request is ${request.exeTime}")

                    try {

                        workload.invoke(request.exeTime)

                        println(" ${clock.millis()} Finished invoke at coroutine ${Thread.currentThread().name} on instance ${getId()}")

                        request.cont.resume(Unit)

                    } catch (cause: CancellationException) {

                        request.cont.resumeWithException(cause)

                        throw cause

                    } catch (cause: Throwable) {

                        request.cont.resumeWithException(cause)

                    }

                }

                state = InstanceState.Idle

            }


        }

    }


    /**
     * run request on instance.
     * if not active, make it active.
     */
    suspend public fun invoke(exeTime: Long){

        // println("MSInstance invoked with id "+ getId()+" at coroutine ${Thread.currentThread().name} ")

        println(" ${clock.millis()} Queuing Invoke request for instance "+ getId() +" with exeTime $exeTime")

        return suspendCancellableCoroutine { cont ->
            queue.add(InvocationRequest(cont, exeTime))
            chan.trySend(Unit)
        }

    }


    /**
     * stop instance, close / remove.
     */
    public suspend fun close(){

        // registryManager.deregisterInstance(this)

        job?.cancel()

        job?.join()

        machine.cancel()

    }


    override fun equals(other: Any?): Boolean = other is MSInstance && id == other.id

    override fun hashCode(): Int = id.hashCode()


    /**
     * A ms invocation request.
     */
    private data class InvocationRequest(val cont: Continuation<Unit>, val exeTime: Long)

}
