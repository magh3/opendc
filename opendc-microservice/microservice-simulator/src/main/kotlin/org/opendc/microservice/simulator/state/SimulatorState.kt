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
import org.opendc.microservice.simulator.router.Request
import org.opendc.microservice.simulator.workload.MSWorkloadMapper
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


public class SimulatorState
    (private val msConfigs: List<MSConfiguration>,
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

    private val depth = 2


    /**
     * The total amount of ms invocations.
     */
    private var totalInvocations:Long = 0

    private var job: Job? = null

    private val queue = ArrayDeque<MSRequest>()

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

                            val exeTime = exePolicy.time()

                            val startTime = clock.millis()

                            loadBalancer.instance(queueEntry.request.ms(), registryManager.getInstances()).
                            invoke(exeTime, queueEntry.request)

                            // resumes when above instance invoke finishes

                            queueEntry.cont.resume(Unit)

                            logger.debug{"--------------finished instance invoke startTime was $startTime"}

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

        var requests: List<Request>

        var nextReqDelay: Long

        val allJobs = mutableListOf<Job>()

        // time loop

            while (clock.millis() < lastReqTime) {

                // get list of microservices

                callMS = routingPolicy.call(registryManager.getMicroservices())

                // convert list of microservices to list of requests

                requests = msToRequests(callMS)

                logger.debug{"Time ${clock.millis()} received request for ${requests.size} microservices"}

                nextReqDelay = interArrivalDelay.time()

                // invoke loop

                allJobs.add(scope.launch {

                    invokeMicroservices(callMS, this)

                })

                delay(nextReqDelay)

        }

        allJobs.joinAll()

        stop()

        val myCollection = registryManager.getInstances()

        val iterator = myCollection.iterator()

        while(iterator.hasNext()) {

            val item = iterator.next()

            logger.info{Arrays.toString(item.getStats().values)}

            item.close()
        }

    }


    /**
     * each call to this function is considered as one request.
     * parameter can have duplicate microservices.
     * Not used for communication as communication require different setting.
     */
    public suspend fun invokeMicroservices(microservices: List<Microservice>, corScope: CoroutineScope){

        // each call to this function is considered as one request

        // can have duplicate microservices

        val requests = msToRequests(microservices)

        val requestJobs = mutableListOf<Job>()

        for (request in requests) {

            totalInvocations += 1

            requestJobs.add(corScope.launch {

                invoke(request)

            })

        }

        requestJobs.joinAll()

        logger.debug{"${clock.millis()} Request completed"}

    }


    private fun msToRequests(microservices: List<Microservice>): List<Request> {

        val requests = mutableListOf<Request>()

        for(ms in microservices){

            requests.add(Request(ms, 0))

        }

        return requests.toList()

    }


    private data class MSRequest(val cont: Continuation<Unit>, val request: Request)


    public suspend fun invoke(request: Request){

        logger.debug{"Current request for ms ${request.ms()}, hop is " + request.getHops()}

        return suspendCancellableCoroutine { cont ->
            queue.add(MSRequest(cont, request))
            chan.trySend(Unit)
        }

    }


    public fun getCommPolicy(): CommunicationPolicy {

        return commPolicy

    }


    public suspend fun stop(){

        job?.cancel()

        job?.join()

    }

}
