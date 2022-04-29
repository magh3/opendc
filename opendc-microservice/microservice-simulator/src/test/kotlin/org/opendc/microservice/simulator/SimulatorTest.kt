package org.opendc.microservice.simulator

import org.junit.jupiter.api.Test
import org.opendc.microservice.simulator.router.ConstForwardPolicy
import org.opendc.microservice.simulator.router.PoissonArrival


internal class SimulatorTest {

    @Test
    fun startTest(){

        val forwardRoutePolicy = ConstForwardPolicy(3)

        val poissonDist = PoissonArrival(5.0)

        var arrivalRequestsNr = poissonDist.getSample()

        if(arrivalRequestsNr == 0){

            println("No request received")

        }
        else if(arrivalRequestsNr > 0){

            println("Received $arrivalRequestsNr requests")

            var forwardNr: Int

            for(reqNr in 1..arrivalRequestsNr){

                println("Looping request $reqNr")

                forwardNr = forwardRoutePolicy.getForwardNr()

                println("Request will be forwarded to $forwardNr microservices")

            }

        }
        else{

            println("Arrival Request error")

            assert(false)

        }

        assert(true)

    }

}
