package org.opendc.microservice.simulator.microservice

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.opendc.microservice.simulator.state.RegistryManager
import org.opendc.microservice.simulator.workload.MSWorkloadMapper
import org.opendc.simulator.compute.SimBareMetalMachine
import org.opendc.simulator.compute.SimMachine
import org.opendc.simulator.compute.model.MachineModel
import org.opendc.simulator.compute.power.ConstantPowerModel
import org.opendc.simulator.compute.power.SimplePowerDriver
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

    private var runningLoadEndTime: Long = 0

    private var state: InstanceState = InstanceState.Idle

    private var exeTime = DescriptiveStatistics().apply{ windowSize = 100 }

    private val queueTime = DescriptiveStatistics().apply{ windowSize = 100 }

    private val slowDown = DescriptiveStatistics().apply{ windowSize = 100 }

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
    public fun totalLoad(): Int {

        var load = 0

        val currentTime = clock.millis()

        // check for remaining running load

        if(runningLoadEndTime > currentTime) load = (runningLoadEndTime - currentTime).toInt()

        // queued load

        load += queueLoad()

        println("Queued load for instance ${getId()} is $load")

        return load

    }


    private fun queueLoad(): Int {

        var load = 0

        for(request in queue){

            load += request.exeTime.toInt()

        }

        return load

    }


    /**
     * connections are nr of requests queued
     */
    public fun activeConnections(): Int {

        var connections = 0

        if(state == InstanceState.Active) connections = queue.size + 1

        else connections = queue.size

        println("Connections for instance ${getId()} is $connections")

        return connections

    }


    public fun getStats(): DescriptiveStatistics {

        return queueTime

    }


    public fun run(){

        job = scope.launch {

            launch{

                machine.startWorkload(workload)

            }

            while (isActive) {
                if (queue.isEmpty()) {
                    chan.receive()
                }


                while (queue.isNotEmpty()) {

                    state = InstanceState.Active

                    println(" ${clock.millis()} Starting queued request at coroutine ${Thread.currentThread().name} on instance ${getId()}")

                    val request = queue.poll()

                    runningLoadEndTime = clock.millis() + request.exeTime

                    println("exeTime of this request is ${request.exeTime}")

                    exeTime.addValue(request.exeTime.toDouble())

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

        println(" ${clock.millis()} Queuing Invoke request for instance "+ getId() +" with exeTime $exeTime")

        val waitTime = queueLoad().toDouble()

        queueTime.addValue(waitTime)

        slowDown.addValue(waitTime+exeTime/exeTime)

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
