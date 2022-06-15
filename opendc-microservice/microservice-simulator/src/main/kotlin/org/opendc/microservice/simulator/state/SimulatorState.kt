package org.opendc.microservice.simulator.state

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.opendc.microservice.simulator.execution.QueuePolicy
import org.opendc.microservice.simulator.router.InterArrivalDelay
import org.opendc.microservice.simulator.loadBalancer.LoadBalancer
import org.opendc.microservice.simulator.microservice.*
import org.opendc.microservice.simulator.router.MSRequest
import org.opendc.microservice.simulator.router.RouterRequest
import org.opendc.microservice.simulator.router.RouterRequestGenerator
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
     private val lastReqTime: Int,
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

    private var exeTimeStat = DescriptiveStatistics().apply{ windowSize = 100 }

    private val queueTimeStat = DescriptiveStatistics().apply{ windowSize = 100 }

    private val totalTimeStat = DescriptiveStatistics().apply{ windowSize = 100 }

    private val slowDownStat = DescriptiveStatistics().apply{ windowSize = 100 }

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

            ms = Microservice((config.getId()), registryManager, lastReqTime)

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

                        launch {

                            val entryRef = queueEntry

                            val startTime = clock.millis()

                            val exeTime = loadBalancer.instance(queueEntry.msReq.getMS(), registryManager.getInstances()).
                            invoke(queueEntry.msReq, queueEntry.request)

                            // resumes when above instance invoke finishes

                            entryRef.cont.resume(exeTime)

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

        // time loop

            while (clock.millis() < lastReqTime) {

                allJobs = allJobs.filter{it.isActive} as MutableList<Job>

                // get request

                val request = requestGenerator.request(registryManager.getMicroservices())

                require(request.getHopMSMap().isNotEmpty()){"Empty request Map"}

                // print("${allJobs.size}, ")

                // println(allJobs.size)

                logger.debug{request}

                nextReqDelay = interArrivalDelay.time()

                // invoke loop

                try {

                    allJobs.add(scope.launch {

                        invokeMicroservices(request, this)

                    })

                }
                catch(e: OutOfMemoryError){

                    println("out of memory error $e")

                    break

                }

                delay(nextReqDelay)

        }

        println("All requests sent waiting for join")

        allJobs.joinAll()

        stop()

        val routerStats = getStats()

        logger.info { routerStats }

        val slaVoilations = routerStats.getTotalTimes().filter{it > 4000.0}.size

        logger.info{"Total sla voilations = $slaVoilations"}

        registryManager.getMicroservices().map{logger.info{"${it.getId()} - ${it.getUtilization()} "}}

        val myCollection = registryManager.getInstances()

        val iterator = myCollection.iterator()

        while(iterator.hasNext()) {

            val item = iterator.next()

            logger.info{item.getStats()}

            item.close()
        }

    }


    /**
     * each call to this function is considered as one request.
     * parameter can have duplicate microservices.
     * Not used for routerMapping as routerMapping require different setting.
     */
    public suspend fun invokeMicroservices(request: RouterRequest, corScope: CoroutineScope){

        // each call to this function is considered as one request

        // can have duplicate microservices

        val startTime = clock.millis()

        val msRequests = request.getInitMSRequests()

        val requestJobs = mutableListOf<Job>()

        var msExeTime = 0

        logger.debug{"Time ${clock.millis()} received request for ${msRequests.size} microservices"}

        for (msReq in msRequests) {

            msReq.getMS().saveExeTime(msReq.getExeTime())

            totalInvocations += 1

            requestJobs.add(corScope.launch {

                msExeTime += invoke(msReq, request)

            })

        }

        requestJobs.joinAll()

        val endTime = clock.millis()

        val totalTime = endTime - startTime

        val waitTime = totalTime - msExeTime

        exeTimeStat.addValue(msExeTime.toDouble())

        queueTimeStat.addValue(waitTime.toDouble())

        totalTimeStat.addValue(totalTime.toDouble()/1000)

        slowDownStat.addValue(((msExeTime+waitTime)/msExeTime).toDouble() )

        logger.debug{"${clock.millis()} Request completed with total time $totalTime, " +
            "execution time was $msExeTime, " +
            "wait time was $waitTime"}

    }


    public fun getStats(): RouterStats {

        return RouterStats(exeTimeStat, queueTimeStat, totalTimeStat, slowDownStat)

    }


    public fun getQueuePolicy(): QueuePolicy {

        return queuePolicy

    }


    private data class MSQRequest(val cont: Continuation<Int>, val msReq: MSRequest, val request: RouterRequest)


    public suspend fun invoke(msReq: MSRequest, request: RouterRequest): Int {

        logger.debug{"Current invoke for ms ${msReq.getMS().getId()}, hop is " + request.getHops()}

        return suspendCancellableCoroutine { cont ->
            queue.add(MSQRequest(cont, msReq, request))
            chan.trySend(Unit)
        }

    }


    public suspend fun stop(){

        job?.cancel()

        job?.join()

    }

}
