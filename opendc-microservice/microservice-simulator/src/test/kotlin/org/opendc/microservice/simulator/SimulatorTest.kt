package org.opendc.microservice.simulator

import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.router.ConstForwardPolicy
import org.opendc.microservice.simulator.router.ForwardPolicy
import org.opendc.microservice.simulator.router.PoissonArrival
import org.opendc.simulator.compute.model.MachineModel
import org.opendc.simulator.compute.model.MemoryUnit
import org.opendc.simulator.compute.model.ProcessingNode
import org.opendc.simulator.compute.model.ProcessingUnit
import org.opendc.simulator.core.runBlockingSimulation
import java.util.*
import org.opendc.compute.workload.telemetry.SdkTelemetryManager
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import org.opendc.compute.workload.topology.HostSpec
import org.opendc.microservice.simulator.microservice.MSConfiguration
import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.MSInstanceDeployer
import org.opendc.microservice.simulator.router.RandomRouting
import org.opendc.microservice.simulator.state.LoadBalancer
import org.opendc.microservice.simulator.state.SimulatorState
import org.opendc.microservice.simulator.workload.MSWorkload
import org.opendc.microservice.simulator.workload.MSWorkloadMapper
import org.opendc.simulator.compute.kernel.SimSpaceSharedHypervisorProvider
import org.opendc.simulator.compute.power.ConstantPowerModel
import org.opendc.simulator.compute.power.SimplePowerDriver
import org.opendc.simulator.compute.workload.SimFlopsWorkload
import org.opendc.simulator.compute.workload.SimWorkload


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
    fun runMiniSim() = runBlockingSimulation {

        val meterProvider = SdkMeterProvider.builder()

        val msConfig = mutableListOf<MSConfiguration>( MSConfiguration(UUID.randomUUID(),
            listOf(UUID.randomUUID())), MSConfiguration(UUID.randomUUID(),
            listOf(UUID.randomUUID())) )

        val workload = spyk(object : MSWorkload, SimWorkload by SimFlopsWorkload(1000) {
            override suspend fun invoke() {
                delay(5000)
            }
        })

        val mapper = object : MSWorkloadMapper {
            override fun createWorkload(msInstance: MSInstance): MSWorkload {
                return workload
            }
        }

        val state = SimulatorState(msConfig, PoissonArrival(5.0),  RandomRouting(), LoadBalancer(),
            clock, this, machineModel, meterProvider.build().get("org.ms.simulator"), mapper)

        state.run(1)

        assert(true)

    }


    @Test
    fun msInstanceDeployTest()= runBlockingSimulation {

        val deployer = MSInstanceDeployer()

        // deployer.deploy(Microservice(UUID.randomUUID()).getId(), UUID.randomUUID())

        assert(true)


    }


    @Test
    fun msConstructTest() = runBlockingSimulation {

        val telemetry = SdkTelemetryManager(clock)
        // val meterProvider = MeterProvider.noop().get("Test 1")
        // val meterProvider = telemetry.createMeterProvider(createHostSpec(1))
        val meterProvider = SdkMeterProvider.builder()

        val microservice = Microservice(UUID.randomUUID())

        println(microservice.getId())

        assert(true)

    }


    /**
     * Construct a [HostSpec] for a simulated host.
     */
    private fun createHostSpec(uid: Int): HostSpec {
        // Machine model based on: https://www.spec.org/power_ssj2008/results/res2020q1/power_ssj2008-20191125-01012.html
        val node = ProcessingNode("AMD", "am64", "EPYC 7742", 32)
        val cpus = List(node.coreCount) { ProcessingUnit(node, it, 3400.0) }
        val memory = List(8) { MemoryUnit("Samsung", "Unknown", 2933.0, 16_000) }

        val machineModel = MachineModel(cpus, memory)

        return HostSpec(
            UUID(0, uid.toLong()),
            "host-$uid",
            emptyMap(),
            machineModel,
            SimplePowerDriver(ConstantPowerModel(0.0)),
            SimSpaceSharedHypervisorProvider()
        )
    }



    @Test
    fun startTest(){

        val forwardRoutePolicy: ForwardPolicy = ConstForwardPolicy(3)

        val poissonDist = PoissonArrival(5.0)

        var arrivalRequestsNr = poissonDist.nrOfRequests()

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
