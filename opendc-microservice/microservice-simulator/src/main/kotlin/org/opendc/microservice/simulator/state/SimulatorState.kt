package org.opendc.microservice.simulator.state

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import org.opendc.microservice.simulator.execution.QueuePolicy
import org.opendc.microservice.simulator.loadBalancer.LoadBalancer
import org.opendc.microservice.simulator.microservice.MSConfiguration
import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.MSInstanceDeployer
import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.router.*
import org.opendc.microservice.simulator.stats.RouterStats
import org.opendc.microservice.simulator.workload.MSWorkloadMapper
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


public class SimulatorState
    (private val msConfigs: List<MSConfiguration>,
     private val requestGenerator: RouterRequestGenerator,
     private val loadBalancer: LoadBalancer,
     private val queuePolicy: QueuePolicy,
     private val clock: Clock,
     private val scope: CoroutineScope,
     private val model: MachineModel,
     private val mapper: MSWorkloadMapper,
     private val lastReqTime: Long,
     private val interArrivalDelay: InterArrivalDelay
) {

    private val deployer =  MSInstanceDeployer()

    /**
     * service discovery
     */
    private val registryManager = RegistryManager()

    /**
     * The total amount of ms invocations.
     */
    private var totalInvocations:Long = 0

    private var job: Job? = null

    private val queue = ArrayDeque<MSQRequest>()

    /**
     * A channel used to signal that new invocations have been enqueued.
     */
    private val chan = Channel<Unit>(Channel.RENDEZVOUS)

    private val logger = KotlinLogging.logger {}

    private val routerStats = RouterStats()

    private val individualExeTimeStats = mutableListOf<Long>()

    private val requestsCompletedHourly = mutableListOf<Long>()

    private var slaVoilations = 0

    private val sla = 4000

    init{

        initializeMS()

        listen()

    }

    /**
     * make ms.
     * make ms instances.
     */
    private fun initializeMS() {

        var ms: Microservice

        for(config in msConfigs){

            // make ms

            ms = Microservice((config.getId()), registryManager, clock, lastReqTime)

            registryManager.addMs(ms)

            // deploy instances

            for(instanceId in config.getInstanceIds()){

                deployer.deploy(ms, instanceId, this, clock, scope, model, registryManager, mapper)

            }

        }

    }


    /**
     * Router that listens to incoming requests
     */
    private fun listen(){

        job = scope.launch {

            while (isActive) {
                if (queue.isEmpty()) {
                    chan.receive()
                }

                while (queue.isNotEmpty()) {

                    val queueEntry = queue.poll()

                    try {

                        // launch coroutine to call microservice and wait for its communications

                        launch {

                            val startTime = clock.millis()

                            val exeTime = loadBalancer.instance(queueEntry.msReq.getMS(), registryManager.getInstances()).
                            invoke(queueEntry.msReq, queueEntry.request)

                            // resumes when above instance invoke finishes

                            queueEntry.cont.resume(exeTime)

                            logger.debug{"--------------finished request startTime was $startTime"}

                        }

                    } catch (cause: CancellationException) {

                        queueEntry.cont.resumeWithException(cause)

                        throw cause

                    } catch (cause: Throwable) {

                        queueEntry.cont.resumeWithException(cause)

                    }

                }



            }


        }

    }


    /**
     * run simulator for t Time unit.
     */
    suspend public fun run(){

        var nextReqDelay: Long

        var allJobs = mutableListOf<Job>()

        var count: Long = 0

        val oneHr = 1*3600*1000

        var utilizationCheckTime = oneHr

        // time loop

            while (clock.millis() < lastReqTime) {

                if(clock.millis() > utilizationCheckTime){

                    println("${clock.millis()} setting utilization")

                    // add utilization

                    registryManager.getMicroservices().map{it.setUtilization()}

                    requestsCompletedHourly.add(count - requestsCompletedHourly.sum())

                    // update timer

                    utilizationCheckTime += oneHr

                }

                count += 1

                allJobs = allJobs.filter{it.isActive} as MutableList<Job>

                // get request

                val request = requestGenerator.request(registryManager.getMicroservices())

                require(request.getHopMSMap().isNotEmpty()){"Empty request Map"}

                // set meta that may be required by queue policy later

                queuePolicy.setMeta(request, sla, clock)

                logger.debug{request}

                nextReqDelay = interArrivalDelay.time()

                // launch coroutine to wait for full request to finish
                // named as full request coroutine

                allJobs.add(scope.launch {

                    invokeMicroservices(request, this)

                })

                delay(nextReqDelay)

        }

        registryManager.getMicroservices().map{it.setUtilization()}

        requestsCompletedHourly.add(count - requestsCompletedHourly.sum())

        logger.info {"All requests sent waiting for join"}

        allJobs.joinAll()

        logger.info {"END TIME ${clock.millis()} \n"}

        logger.info {"Total Nr of requests: $count \n"}

        logger.info {"Hourly requests: $requestsCompletedHourly  \n"}

        logger.info {"$routerStats" }

        logger.info{"Total sla voilations = $slaVoilations \n"}

        logger.info{"Individual ms exe times: $individualExeTimeStats \n"}

        logger.info { "Hourly Utilization: \n" }
        registryManager.getMicroservices().map {
            logger.info {
                "ms: ${it.getId()}  mean is ${it.getUtilization().average()}" +
                    " with values  ${it.getUtilization().contentToString()} \n"
            }
        }

        registryManager.getInstances().map{logger.info{it.getStats()}}

        stop(registryManager)

    }


    /**
     * each call to this function is considered as one request.
     * parameter can have duplicate microservices.
     * Not used for communication as communication require different setting.
     */
    public suspend fun invokeMicroservices(request: RouterRequest, corScope: CoroutineScope){

        // each call to this function is considered as one request

        // can have duplicate microservices

        val startTime = clock.millis()

        val msRequests = request.getInitMSRequests()

        val requestJobs = mutableListOf<Job>()

        var msExeTime: Long = 0

        logger.debug{"Time ${clock.millis()} received request for ${msRequests.size} microservices"}

        for (msReq in msRequests) {

            // msReq.getMS().saveExeTime(msReq.getExeTime())

            totalInvocations += 1

            // launch coroutine to invoke individual microservices from the request

            requestJobs.add(corScope.launch {

                msExeTime += invoke(msReq, request)

            })

        }

        requestJobs.joinAll()

        val endTime = clock.millis()

        val totalTime = endTime - startTime

        if(totalTime > sla) slaVoilations += 1

        val waitTime = totalTime - msExeTime

        routerStats.saveExeTime(msExeTime/1000)

        routerStats.saveWaitTime(waitTime/1000)

        routerStats.saveTotalTime(totalTime/1000)

        routerStats.saveSlowDown(((msExeTime+waitTime)/msExeTime) )

        logger.debug{"${clock.millis()} Request completed with total time $totalTime, " +
            "execution time was $msExeTime, " +
            "wait time was $waitTime"}

    }


    public fun getQueuePolicy(): QueuePolicy {

        return queuePolicy

    }


    private data class MSQRequest(val cont: Continuation<Int>, val msReq: MSRequest, val request: RouterRequest)


    public suspend fun invoke(msReq: MSRequest, request: RouterRequest): Int {

        msReq.getMS().saveExeTime(msReq.getExeTime())

        individualExeTimeStats.add((msReq.getExeTime()/1000))

        logger.debug{"Current invoke for ms ${msReq.getMS().getId()}, hop is " + request.getHops()}

        // suspend the individual ms invoke coroutine

        return suspendCancellableCoroutine { cont ->
            queue.add(MSQRequest(cont, msReq, request))
            chan.trySend(Unit)
        }

    }


    public suspend fun stop(registryManager: RegistryManager){

        // stop instances

        val instances = registryManager.getInstances().toList()

        instances.map{registryManager.deregisterInstance(it)}

        // stop router

        job?.cancel()

        job?.join()

    }

}
