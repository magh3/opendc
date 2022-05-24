package org.opendc.microservice.simulator.state

import io.opentelemetry.api.metrics.Meter
import kotlinx.coroutines.*
import org.opendc.microservice.simulator.execution.ExeDelay
import org.opendc.microservice.simulator.execution.InterArrivalDelay
import org.opendc.microservice.simulator.loadBalancer.LoadBalancer
import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.microservice.MSConfiguration
import org.opendc.microservice.simulator.microservice.MSInstanceDeployer
import org.opendc.microservice.simulator.router.PoissonArrival
import org.opendc.microservice.simulator.mapping.RoutingPolicy
import org.opendc.microservice.simulator.workload.MSWorkloadMapper
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock


public class SimulatorState
    (private val msConfigs: List<MSConfiguration>,
     private val routingPolicy: RoutingPolicy,
     private val loadBalancer: LoadBalancer,
     private val exePolicy: ExeDelay,
     private val clock: Clock,
     private val scope: CoroutineScope,
     private val model: MachineModel,
     private val meter: Meter,
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
    private val _invocations = meter.counterBuilder("state.invocations.total")
        .setDescription("Number of ms invocations")
        .setUnit("1")
        .build()


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

                deployer.deploy(ms.getId(), instanceId, clock, scope, model, registryManager, mapper)

            }

        }

        return msList

    }


    /**
     * run simulator for t Time unit.
     */
    suspend public fun run(){

        // var nrOfRequests = 0

        // val poissonArrival = PoissonArrival(5.0)

        var callMS: List<Microservice>

        var exeTime: Long

        var nextReqDelay: Long

        // time loop

        coroutineScope {


            while (clock.millis() < lastReqTime) {

                println("Request received at time ${clock.millis()}")

                callMS = routingPolicy.call(microservices, 1)

                nextReqDelay = interArrivalDelay.time()

                // invoke loop

                for (ms in callMS) {

                    _invocations.add(1)

                    exeTime = exePolicy.time()

                    launch {

                        loadBalancer.instance(ms, registryManager.getInstances()).invoke(exeTime)

                    }


                }

                delay(nextReqDelay)

            }

        }

        val myCollection = registryManager.getInstances()

        val iterator = myCollection.iterator()

        while(iterator.hasNext()) {

            val item = iterator.next()

            item.close()
        }

    }

}
