package org.opendc.microservice.simulator.microservice

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.opendc.microservice.simulator.execution.RequestExecution
import org.opendc.microservice.simulator.router.MSRequest
import org.opendc.microservice.simulator.router.RouterRequest
import org.opendc.microservice.simulator.state.RegistryManager
import org.opendc.microservice.simulator.state.SimulatorState
import org.opendc.microservice.simulator.stats.MSInstanceStats
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

    private var queue = ArrayDeque<InvocationRequest>()

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

    private var instanceStats = MSInstanceStats(getMSId(), getId())

    private val reqExecution: RequestExecution = simState.getReqExe()

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
    public fun totalLoad(): Long {

        var load: Long = 0

        val currentTime = clock.millis()

        // check for remaining running load

        if(runningLoadEndTime > currentTime) load = (runningLoadEndTime - currentTime)

        // queued load

        load += queueLoad()

        logger.debug {"Queued load for instance ${getId()} is $load"}

        return load

    }


    private fun queueLoad(): Int {

        var load = 0

        for(request in queue){

            load += request.msReq.getExeTime().toInt()

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


    public fun getStats(): MSInstanceStats {

        return instanceStats

    }


    public fun run(){

        job = scope.launch {

            launch{

                machine.startWorkload(workload)

            }

            var allJobs = mutableListOf<Job>()

            while (isActive) {
                if (queue.isEmpty()) {
                    chan.receive()
                }


                while (queue.isNotEmpty()) {

                    allJobs = allJobs.filter{it.isActive} as MutableList<Job>

                    state = InstanceState.Active

                    queue = simState.getQueuePolicy().getEntry(queue) as ArrayDeque<InvocationRequest>

                    // queue.map{println(it.msReq.getMeta()["stageDeadline"])}

                    queue.map{println(it.msReq.getExeTime())}

                    val queueEntry = queue.poll()

                    // if(queue.size > 500)
                    // println("____Queue size ${queue.size} at instance ${getId()} ms ${getMSId()}")

                    // println("queue size ${queue.size +1}, choosed req with deadline ${queueEntry.msReq.getMeta()["stageDeadline"]}" )

                    val exeTime = queueEntry.msReq.getExeTime()

                    runningLoadEndTime = clock.millis() + exeTime

                    logger.debug{" ${clock.millis()} Starting queued request at coroutine ${Thread.currentThread().name}" +
                        " on instance ${getId()}, exeTime of this request is ${exeTime}"}

                    workload.invoke()

                    val exeFinished = reqExecution.execute(queueEntry)

                    if(exeFinished) {

                        // request completes

                        allJobs.add(launch {

                            communicate(this, queueEntry.request, queueEntry.msReq)

                        })

                        logger.debug { " ${clock.millis()} Finished invoke at coroutine ${Thread.currentThread().name} on instance ${getId()}" }

                    }
                    else{

                        // if request not finished put back to queue

                        queue.add(queueEntry)

                    }

                }

                state = InstanceState.Idle

            }

            allJobs.joinAll()

        }

    }


    suspend private fun communicate(corScope: CoroutineScope, request: RouterRequest, msReq: MSRequest) {

        val hopsDone = request.getHops()

        var commExeTime = 0

        // check if there are ms to communicate

        val commRequests = request.getCommRequests(hopsDone, msReq)

        // no duplicates

        require(commRequests.distinct().size == commRequests.size){"Communication should have distinct MS"}

        // should not contain self

        require(!commRequests.contains(msReq)){"Communication to self not allowed"}

        logger.debug{"${clock.millis()} instance ${getId()} communicating with ${commRequests.size} ms $commRequests"}

        if (commRequests.isEmpty()) {

            // no routerMapping. Finish this request coroutine

            resumeCoroutine(msReq.getCont(), msReq.getExeTime())

            return

        }

        val allJobs = mutableListOf<Job>()

        for (commReq in commRequests) {

            // require(commReq.getMeta()["stageDeadline"]  != null){"Error, Stage deadline is null"}

            allJobs.add(corScope.launch {

                val nextHop = hopsDone + 1

                commExeTime += simState.invoke(commReq, RouterRequest(nextHop, request.getHopMSMap()))

            })

        }

        // track launched coroutines and resume only on done that is when routerMapping done coroutine finish.

        allJobs.joinAll()

        resumeCoroutine(msReq.getCont(), msReq.getExeTime())

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
    public suspend fun invoke(msReq: MSRequest ,request: RouterRequest): Int{

        val exeTime = msReq.getExeTime()

        // record execution time

        instanceStats.saveExeTime(exeTime)

        logger.debug{" ${clock.millis()} Queuing Invoke request for instance "+ getId() +" with exeTime $exeTime"}

        val waitTime = totalLoad()

        // record wait time

        instanceStats.saveWaitTime(waitTime)

        // record total time
        instanceStats.saveTotalTime(waitTime+exeTime)

        // record slowdown

        instanceStats.saveSlowDown( ( (waitTime+exeTime)/exeTime) )

        return suspendCancellableCoroutine { cont ->
            msReq.setCont(cont)
            queue.add(InvocationRequest(msReq, request))
            chan.trySend(Unit)
        }

    }


    /**
     * stop instance, close / remove.
     * called by registry manager when deregister called
     */
    public suspend fun close(){

        job?.cancel()

        job?.join()

        machine.cancel()

    }


    override fun equals(other: Any?): Boolean = other is MSInstance && id == other.id

    override fun hashCode(): Int = id.hashCode()


    /**
     * A ms invocation request.
     */
    public data class InvocationRequest( val msReq: MSRequest, val request: RouterRequest)

}
