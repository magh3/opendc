package org.opendc.microservice.simulator

import io.opentelemetry.api.metrics.MeterProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opendc.microservice.simulator.mapping.RandomMicroserviceMapper
import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.microservice.MicroserviceConfiguration
import org.opendc.microservice.simulator.microservice.MicroserviceInstance
import org.opendc.microservice.simulator.router.ConstForwardPolicy
import org.opendc.microservice.simulator.router.ForwardPolicy
import org.opendc.microservice.simulator.router.PoissonArrival
import org.opendc.microservice.simulator.state.SimulatorInitializer
import org.opendc.simulator.compute.model.MachineModel
import org.opendc.simulator.compute.model.MemoryUnit
import org.opendc.simulator.compute.model.ProcessingNode
import org.opendc.simulator.compute.model.ProcessingUnit
import org.opendc.simulator.core.runBlockingSimulation
import java.util.*


internal class SimulatorTest {

    private lateinit var machineModel: MachineModel

    @BeforeEach
    fun setUp() {
        val cpuNode = ProcessingNode("Intel", "Xeon", "amd64", 2)

        machineModel = MachineModel(
            cpus = List(cpuNode.coreCount) { ProcessingUnit(cpuNode, it, 1000.0) },
            memory = List(4) { MemoryUnit("Crucial", "MTA18ASF4G72AZ-3G2B1", 3200.0, 32_000) }
        )
    }


    @Test
    fun msInstanceConstructTest()= runBlockingSimulation {

        val instance = MicroserviceInstance(UUID.randomUUID(), clock, this, machineModel)

                

    }



    @Test
    fun microserviceConstructTest() = runBlockingSimulation {

        val microservice = Microservice(UUID.randomUUID(), 1, clock, this,
            machineModel, MeterProvider.noop().get("Test 1"))

        microservice.invocations.add(1)

        println(microservice.getId())

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
