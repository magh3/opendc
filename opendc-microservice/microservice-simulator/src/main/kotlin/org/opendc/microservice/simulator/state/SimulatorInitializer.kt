package org.opendc.microservice.simulator.state

import kotlinx.coroutines.CoroutineScope
import org.opendc.microservice.simulator.mapping.MicroserviceMapPolicy
import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.microservice.MicroserviceConfiguration
import org.opendc.microservice.simulator.microservice.MicroserviceGenerator
import org.opendc.microservice.simulator.router.ForwardPolicy
import org.opendc.microservice.simulator.router.PoissonArrival
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock


public class SimulatorInitializer
    (private val microserviceConfig: MicroserviceConfiguration,
     private val requestRate: Double,
     private val forwardPolicy: ForwardPolicy,
     private val microserviceMapper: MicroserviceMapPolicy,
     private val clock: Clock,
     private val scope: CoroutineScope,
     private val model: MachineModel
     ) {

    private val microservices = initializeMicroservices()

    private fun initializeMicroservices(): MutableList<Microservice> {

        return MicroserviceGenerator(clock, scope, model).generate(mutableListOf<MicroserviceConfiguration>())

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
