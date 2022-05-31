package org.opendc.microservice.simulator.state

import io.opentelemetry.api.metrics.Meter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.opendc.microservice.simulator.execution.ExeDelay
import org.opendc.microservice.simulator.execution.InterArrivalDelay
import org.opendc.microservice.simulator.loadBalancer.LoadBalancer
import org.opendc.microservice.simulator.router.PoissonArrival
import org.opendc.microservice.simulator.mapping.RoutingPolicy
import org.opendc.microservice.simulator.microservice.*
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

    private val microservices = initializeMS()


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

    init{

        listen()

    }

    /**
     * make ms.
     * make ms instances.
     */
    private fun initializeMS(): MutableList<Microservice> {

        val msList = mutableListOf<Microservice>()

        var ms: Microservice

        for(config in msConfigs){

            // make ms

            ms = Microservice((config.getId()))

            msList.add(ms)

            // deploy instances

            for(instanceId in config.getInstanceIds()){

                deployer.deploy(ms.getId(), instanceId, this, clock, scope, model, registryManager, mapper)

            }

        }

        return msList

    }

    /**
     * listens to incoming requests
     */
    private fun listen(){

        job = scope.launch {

            while (isActive) {
                if (queue.isEmpty()) {
                    chan.receive()
                }

                while (queue.isNotEmpty()) {

                    val request = queue.poll()

                    val exeTime = exePolicy.time()

                    try {

                        launch {

                            loadBalancer.instance(request.ms, registryManager.getInstances())
                                .invoke(exeTime)

                            request.cont.resume(Unit)

                            println("--------------finished request")

                        }

                    } catch (cause: CancellationException) {

                        request.cont.resumeWithException(cause)

                        throw cause

                    } catch (cause: Throwable) {

                        request.cont.resumeWithException(cause)

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

        var exeTime: Long

        var nextReqDelay: Long

        // time loop

        coroutineScope {

            while (clock.millis() < lastReqTime) {

                exeTime = exePolicy.time()

                callMS = routingPolicy.call(microservices)

                println("Request received at time ${clock.millis()} for ${callMS.size} microservices")

                nextReqDelay = interArrivalDelay.time()

                // invoke loop

                for (ms in callMS) {

                    totalInvocations += 1

                    launch{

                        invoke(ms)

                    }

                }

                delay(nextReqDelay)

            }

        }

        val myCollection = registryManager.getInstances()

        val iterator = myCollection.iterator()

        while(iterator.hasNext()) {

            val item = iterator.next()

            println(Arrays.toString(item.getStats().values))

            item.close()
        }

    }


    private data class MSRequest(val cont: Continuation<Unit>, val ms: Microservice)


    suspend public fun invoke(ms: Microservice){

        println(" ${clock.millis()}  Invoke request for MS ${ms.getId()}")

        return suspendCancellableCoroutine { cont ->
            queue.add(MSRequest(cont, ms))
            chan.trySend(Unit)
        }

    }


    public suspend fun stop(){

        job?.cancel()

        job?.join()

    }

}
