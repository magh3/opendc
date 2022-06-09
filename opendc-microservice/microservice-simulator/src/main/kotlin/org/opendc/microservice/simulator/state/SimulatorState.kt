package org.opendc.microservice.simulator.state

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import org.opendc.microservice.simulator.communication.CommunicationPolicy
import org.opendc.microservice.simulator.execution.ExeDelay
import org.opendc.microservice.simulator.execution.InterArrivalDelay
import org.opendc.microservice.simulator.loadBalancer.LoadBalancer
import org.opendc.microservice.simulator.mapping.RoutingPolicy
import org.opendc.microservice.simulator.microservice.*
import org.opendc.microservice.simulator.router.MSRequest
import org.opendc.microservice.simulator.router.RouterRequest
import org.opendc.microservice.simulator.router.RouterRequestGeneratorImpl
import org.opendc.microservice.simulator.workload.MSWorkloadMapper
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


public class SimulatorState
    (private val msConfigs: List<MSConfiguration>,
     private val depth: Int,
     private val routingPolicy: RoutingPolicy,
     private val loadBalancer: LoadBalancer,
     private val exePolicy: ExeDelay,
     private val commPolicy: CommunicationPolicy,
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

            ms = Microservice((config.getId()))

            registryManager.addMs(ms)

            // deploy instances

            for(instanceId in config.getInstanceIds()){

                deployer.deploy(ms, instanceId, this, clock, scope, model, registryManager, mapper)

            }

        }

    }


    public fun getDepth(): Int {

        return depth

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

                            val startTime = clock.millis()

                            val exeTime = loadBalancer.instance(queueEntry.msReq.getMS(), registryManager.getInstances()).
                            invoke(queueEntry.msReq, queueEntry.request)

                            // resumes when above instance invoke finishes

                            queueEntry.cont.resume(Unit)

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

        var callMS: List<Microservice>

        var requests: List<MSRequest>

        var nextReqDelay: Long

        val allJobs = mutableListOf<Job>()

        var request: RouterRequest

        // time loop

            while (clock.millis() < lastReqTime) {

                // get request

                request = RouterRequestGeneratorImpl(commPolicy, exePolicy)
                    .request(registryManager.getMicroservices())

                require(request.getHopMSMap().isNotEmpty()){"Empty request Map"}

                println(request)

                nextReqDelay = interArrivalDelay.time()

                // invoke loop

                allJobs.add(scope.launch {

                    invokeMicroservices(request, this)

                })

                delay(nextReqDelay)

        }

        allJobs.joinAll()

        stop()

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
     * Not used for communication as communication require different setting.
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

            totalInvocations += 1

            requestJobs.add(corScope.launch {

                invoke(msReq, request)

            })

        }

        requestJobs.joinAll()

        val endTime = clock.millis()

        val totalTime = endTime - startTime

        val waitTime = totalTime - msExeTime

        logger.debug{"${clock.millis()} Request completed with total time $totalTime, " +
            "execution time was $msExeTime, " +
            "wait time was $waitTime"}

    }



    private data class MSQRequest(val cont: Continuation<Unit>, val msReq: MSRequest, val request: RouterRequest)


    public suspend fun invoke(msReq: MSRequest, request: RouterRequest) {

        logger.debug{"Current invoke for ms ${msReq.getMS().getId()}, hop is " + request.getHops()}

        return suspendCancellableCoroutine { cont ->
            queue.add(MSQRequest(cont, msReq, request))
            chan.trySend(Unit)
        }

    }


    public fun getCommPolicy(): CommunicationPolicy {

        return commPolicy

    }


    public fun getExePolicy(): ExeDelay {

        return exePolicy

    }


    public suspend fun stop(){

        job?.cancel()

        job?.join()

    }

}
