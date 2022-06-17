package org.opendc.microservice.simulator.state

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.opendc.microservice.simulator.execution.QueuePolicy
import org.opendc.microservice.simulator.loadBalancer.LoadBalancer
import org.opendc.microservice.simulator.microservice.MSConfiguration
import org.opendc.microservice.simulator.microservice.MSInstanceDeployer
import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.router.*
import org.opendc.microservice.simulator.routerMapping.RouterHelper
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

    private var exeTimeStat = DescriptiveStatistics().apply{ windowSize = 100 }

    private val queueTimeStat = DescriptiveStatistics().apply{ windowSize = 100 }

    private val totalTimeStat = DescriptiveStatistics().apply{ windowSize = 100 }

    private val slowDownStat = DescriptiveStatistics().apply{ windowSize = 100 }

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

        var count = 0

        val oneHr = 1*3600*1000

        var utilizationCheckTime = oneHr

        // time loop

            while (clock.millis() < lastReqTime) {

                // println("filtering..")

                if(clock.millis() > utilizationCheckTime){

                    println("${clock.millis()} setting utilization")

                    // add utilization

                    registryManager.getMicroservices().map{it.setUtilization()}

                    // update timer

                    utilizationCheckTime += oneHr

                }

                count += 1

                allJobs = allJobs.filter{it.isActive} as MutableList<Job>

                // get request

                val request = requestGenerator.request(registryManager.getMicroservices())

                // RouterHelper().setExeBasedDeadline(request, sla)

                require(request.getHopMSMap().isNotEmpty()){"Empty request Map"}

                // print("${allJobs.size}, ")

                // println("total requests: $count")

                // println("${clock.millis()} - request remaining: ${allJobs.size}")

                logger.debug{request}

                nextReqDelay = interArrivalDelay.time()

                // invoke loop

                try {

                    // launch coroutine to wait for full request to finish
                    // named as full request coroutine

                    allJobs.add(scope.launch {

                        // try {
                            // withTimeout((1000 * 3600).toLong()) {

                                invokeMicroservices(request, this)

                            // }
                        // }
                        // catch(t: TimeoutCancellationException){

                        //     println("request timeout ")

                        // }

                        // println("Request completed memory free is " +
                        //     formatSize(Runtime.getRuntime().freeMemory()) + " / " +
                        //     formatSize(Runtime.getRuntime().maxMemory())
                        // )

                    })


                }
                catch(e: OutOfMemoryError){

                    logger.error {"out of memory error $e"}

                    break

                }

                delay(nextReqDelay)

        }

        println("All requests sent waiting for join")

        allJobs.joinAll()

        println("END TIME ${clock.millis()}")

        stop()

        val routerStats = getStats()

        logger.info { routerStats }

        // val slaVoilations = routerStats.getTotalTimes().filter{it > 4000.0}.size

        logger.info{"Total sla voilations = $slaVoilations"}

        registryManager.getMicroservices().map{logger.info{"${it.getId()} -  ${it.getUtilization().contentToString()} so mean is ${it.getUtilization().average()}"}}

        val myCollection = registryManager.getInstances()

        val iterator = myCollection.iterator()

        while(iterator.hasNext()) {

            val item = iterator.next()

            logger.info{item.getStats()}

            item.close()
        }

    }


    public fun formatSize(v: Long): String {
        if (v < 1024) return "$v B"
        val z = (63 - java.lang.Long.numberOfLeadingZeros(v)) / 10
        return String.format("%.1f %sB", v.toDouble() / (1L shl z * 10), " KMGTPE"[z])
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

        // println("hop is " + request.getHops())

        msReq.getMS().saveExeTime(msReq.getExeTime())

        logger.debug{"Current invoke for ms ${msReq.getMS().getId()}, hop is " + request.getHops()}

        // suspend the individual ms invoke coroutine

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
