package org.opendc.microservice.simulator.microservice

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.opendc.microservice.simulator.execution.ExeDelay
import org.opendc.microservice.simulator.router.Request
import org.opendc.microservice.simulator.state.RegistryManager
import org.opendc.microservice.simulator.state.SimulatorState
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
public class MSInstance(private val ms: Microservice,
                        private val id: UUID,
                        private val simState: SimulatorState,
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

    private var exeTimeStat = DescriptiveStatistics().apply{ windowSize = 100 }

    private val queueTimeStat = DescriptiveStatistics().apply{ windowSize = 100 }

    private val slowDownStat = DescriptiveStatistics().apply{ windowSize = 100 }

    private val commPolicy = simState.getCommPolicy()

    private val exePolicy = simState.getExePolicy()

    /**
     * The logger instance of this instance.
     */
    private val logger = KotlinLogging.logger {}

    init{

        registryManager.registerInstance(this)

    }


    public fun getId(): UUID {

        return id

    }


    public fun getMSId(): UUID {

        return ms.getId()

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

        logger.debug {"Queued load for instance ${getId()} is $load"}

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

        logger.debug{"Connections for instance ${getId()} is $connections"}

        return connections

    }


    public fun getStats(): DescriptiveStatistics {

        return queueTimeStat

    }


    public fun run(){

        job = scope.launch {

            launch{

                machine.startWorkload(workload)

            }

            val allJobs = mutableListOf<Job>()

            while (isActive) {
                if (queue.isEmpty()) {
                    chan.receive()
                }


                while (queue.isNotEmpty()) {

                    state = InstanceState.Active

                    val request = queue.poll()

                    runningLoadEndTime = clock.millis() + request.exeTime

                    logger.debug{" ${clock.millis()} Starting queued request at coroutine ${Thread.currentThread().name}" +
                        " on instance ${getId()}, exeTime of this request is ${request.exeTime}"}

                    exeTimeStat.addValue(request.exeTime.toDouble())

                    workload.invoke()

                    delay(request.exeTime)

                    allJobs.add(launch {

                        communicate(this, request.request, request.exeTime)

                    })

                    logger.debug { " ${clock.millis()} Finished invoke at coroutine ${Thread.currentThread().name} on instance ${getId()}"}

                }

                state = InstanceState.Idle

            }

            allJobs.joinAll()

        }

    }


    suspend private fun communicate(corScope: CoroutineScope, currentRequest: Request, exeTime: Long) {

        val hopsDone = currentRequest.getHops()

        var commExeTime = 0

        // check depth. if depth reached no communicate

        if (hopsDone < simState.getDepth()) {

            // depth not reached

            val callMS = commPolicy.communicateMs(ms, hopsDone, registryManager.getMicroservices())

            // no duplicates

            require(callMS.distinct().size == callMS.size){"Communication should have distinct MS"}

            // should not contain self

            require(!callMS.contains(ms)){"Communication to self not allowed"}

            logger.debug{"${clock.millis()} instance ${getId()} communicating with ${callMS.size} ms $callMS"}

            if (callMS.isEmpty()) {

                // no communication. Finish this request coroutine

                resumeCoroutine(currentRequest.getCont(), exeTime)

                return

            }

            val allJobs = mutableListOf<Job>()

            for (microservice in callMS) {

                allJobs.add(corScope.launch {

                    val nextHop = hopsDone + 1

                    commExeTime += simState.invoke(Request(microservice, nextHop))

                })

            }

            // track launched coroutines and resume only on done that is when communication done coroutine finish.

            allJobs.joinAll()

        }

        // max depth reached or communication joined

        resumeCoroutine(currentRequest.getCont(), exeTime + commExeTime)

    }


    /**
     * finish request coroutine
     */
    private fun resumeCoroutine(cont: Continuation<Int>, exeTime: Long){

        try {
            cont.resume(exeTime.toInt())

        } catch (cause: CancellationException) {

            cont.resumeWithException(cause)

            throw cause

        } catch (cause: Throwable) {

            cont.resumeWithException(cause)

        }

    }


    /**
     * run request on instance.
     * if not active, make it active.
     */
    public suspend fun invoke(request: Request): Int{

        val exeTime = exePolicy.time(ms, request.getHops())

        logger.debug{" ${clock.millis()} Queuing Invoke request for instance "+ getId() +" with exeTime $exeTime"}

        val waitTime = queueLoad().toDouble()

        queueTimeStat.addValue(waitTime)

        slowDownStat.addValue(waitTime+exeTime/exeTime)

        return suspendCancellableCoroutine { cont ->
            request.setCont(cont)
            queue.add(InvocationRequest(cont, exeTime, request))
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
    private data class InvocationRequest(val cont: Continuation<Int>, val exeTime: Long, val request: Request)

}
