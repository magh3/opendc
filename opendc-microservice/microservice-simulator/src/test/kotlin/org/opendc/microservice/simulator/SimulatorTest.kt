package org.opendc.microservice.simulator

import org.junit.jupiter.api.Test
import org.opendc.microservice.simulator.mapping.RandomMicroserviceMapper
import org.opendc.microservice.simulator.microservice.MicroservicesConfiguration
import org.opendc.microservice.simulator.router.ConstForwardPolicy
import org.opendc.microservice.simulator.router.ForwardPolicy
import org.opendc.microservice.simulator.router.PoissonArrival
import org.opendc.microservice.simulator.state.SimulatorInitializer


internal class SimulatorTest {

    @Test
    fun Test2(){

        val microservicesConfig = MicroservicesConfiguration(arrayOf("M1"), arrayOf(1))

        val simulatorInitializer = SimulatorInitializer(microservicesConfig, 5.0, ConstForwardPolicy(3)
        , RandomMicroserviceMapper())

        simulatorInitializer.runSimulator(1)

        assert(true)

    }

    @Test
    fun startTest(){

        val forwardRoutePolicy: ForwardPolicy = ConstForwardPolicy(3)

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
