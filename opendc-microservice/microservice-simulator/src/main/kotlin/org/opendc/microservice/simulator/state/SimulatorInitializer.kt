package org.opendc.microservice.simulator.state

import io.opentelemetry.api.metrics.Meter
import kotlinx.coroutines.CoroutineScope
import org.opendc.microservice.simulator.mapping.MicroserviceMapPolicy
import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.microservice.MSConfiguration
import org.opendc.microservice.simulator.microservice.MSInstanceDeployer
import org.opendc.microservice.simulator.microservice.MicroserviceInstance
import org.opendc.microservice.simulator.router.ForwardPolicy
import org.opendc.microservice.simulator.router.RoutingPolicy
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock


public class SimulatorInitializer
    (private val msConfigs: List<MSConfiguration>,
     private val routingPolicy: RoutingPolicy,
     private val clock: Clock,
     private val scope: CoroutineScope,
     private val model: MachineModel,
     private val meter: Meter
     ) {

    private val microservices = initializeMS()

    private val deployer =  MSInstanceDeployer()

    /**
     * service discovery
     */
    private val registry = mutableListOf<MicroserviceInstance>()

    
    private fun initializeMS(): MutableList<Microservice> {

        val msList = mutableListOf<Microservice>()

        var ms: Microservice

        for(config in msConfigs){

            ms = Microservice((config.getId()))

            msList.add(ms)

            for(instanceId in config.getInstanceIds()){

                registry.add(deployer.deploy(ms.getId(), instanceId, clock, scope, model))

            }

        }

        return msList

    }

    /*

    /*
    * runTime: time in sec to run the simulator
    * */
    public fun runSimulator(runTime: Int){

        for(i in 0 until runTime) simulateOneSecRequests()

    }


    private fun simulateOneSecRequests(){

        val poissonDist = PoissonArrival(requestRate)

        var arrivalAmount = poissonDist.getSample()

        if(arrivalAmount == 0){

            println("No request received")

        }
        else if(arrivalAmount > 0){

            println("Received $arrivalAmount requests")

            var forwardNr: Int

            for(reqNr in 1..arrivalAmount){

                println("Looping request $reqNr")

                forwardNr = forwardPolicy.getForwardNr()

                println("Request will be forwarded to $forwardNr microservices")

            }

        }
        else{

            println("Arrival Request error")

        }

    }

     */

}
