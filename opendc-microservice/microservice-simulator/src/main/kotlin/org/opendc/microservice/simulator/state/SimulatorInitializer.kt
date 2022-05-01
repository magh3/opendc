package org.opendc.microservice.simulator.state

import org.opendc.microservice.simulator.mapping.MicroserviceMapPolicy
import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.microservice.MicroservicesConfiguration
import org.opendc.microservice.simulator.microservice.MicroserviceGenerator
import org.opendc.microservice.simulator.router.ForwardPolicy
import org.opendc.microservice.simulator.router.PoissonArrival


public class SimulatorInitializer
    (private val microserviceConfig: MicroservicesConfiguration,
     private val requestRate: Double,
     private val forwardPolicy: ForwardPolicy,
     private val microserviceMapper: MicroserviceMapPolicy,
     ) {

    private val microservices = initializeMicroservices()

    private fun initializeMicroservices(): Array<Microservice> {

        return MicroserviceGenerator().generate(microserviceConfig.ids, microserviceConfig.instances)

    }


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

}
