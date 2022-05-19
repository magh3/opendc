package org.opendc.microservice.simulator.microservice

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


    public fun run(){

        job = scope.launch {

            launch{

                println(" ${clock.millis()} launching instance with UUID "+getId()+" at coroutine ${Thread.currentThread().name} ")

                machine.startWorkload(workload)

                println(" ${clock.millis()} Finished instance workload"+" at coroutine ${Thread.currentThread().name}" )


            }

            while (isActive) {
                if (queue.isEmpty()) {
                    chan.receive()
                }

                println(" ${clock.millis()} Invoke request received at coroutine ${Thread.currentThread().name}")

                while (queue.isNotEmpty()) {

                    println(" ${clock.millis()} Found in queue at coroutine ${Thread.currentThread().name}")

                    val request = queue.poll()
                    try {
                        workload.invoke()
                        request.cont.resume(Unit)
                    } catch (cause: CancellationException) {
                        request.cont.resumeWithException(cause)
                        throw cause
                    } catch (cause: Throwable) {
                        request.cont.resumeWithException(cause)
                    }
                }

                println(" ${clock.millis()} Invoke finished at coroutine ${Thread.currentThread().name}")

            }


        }

    }


    /**
     * run request on instance.
     * if not active, make it active.
     */
    suspend public fun invoke(){

        // println("MSInstance invoked with id "+ getId()+" at coroutine ${Thread.currentThread().name} ")

        println(" ${clock.millis()} Queuing Invoke request")

        return suspendCancellableCoroutine { cont ->
            queue.add(InvocationRequest(cont))
            chan.trySend(Unit)
        }

    }


    /**
     * stop instance, close / remove.
     */
    public fun close(){

        registryManager.deregisterInstance(this)

    }


    override fun equals(other: Any?): Boolean = other is MSInstance && id == other.id

    override fun hashCode(): Int = id.hashCode()


    /**
     * A function invocation request.
     */
    private data class InvocationRequest(val cont: Continuation<Unit>)

}
