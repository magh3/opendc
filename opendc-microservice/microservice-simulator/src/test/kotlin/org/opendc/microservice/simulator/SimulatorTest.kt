package org.opendc.microservice.simulator

import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opendc.simulator.compute.model.MachineModel
import org.opendc.simulator.compute.model.MemoryUnit
import org.opendc.simulator.compute.model.ProcessingNode
import org.opendc.simulator.compute.model.ProcessingUnit
import org.opendc.simulator.core.runBlockingSimulation
import org.opendc.microservice.simulator.execution.*
import org.opendc.microservice.simulator.execution.order.FirstComeFirstServe
import org.opendc.microservice.simulator.routerMapping.ProbRouting
import org.opendc.microservice.simulator.router.PoissonDelay
import org.opendc.microservice.simulator.loadBalancer.*
import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.MSConfigGenerator
import org.opendc.microservice.simulator.router.ProbDepthPolicy
import org.opendc.microservice.simulator.router.RouterRequestGeneratorImpl
import org.opendc.microservice.simulator.state.SimulatorState
import org.opendc.microservice.simulator.workload.MSWorkload
import org.opendc.microservice.simulator.workload.MSWorkloadMapper
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

        val msConfig = MSConfigGenerator().generate(4,listOf(4,1,1,2))
        val workload = spyk(object : MSWorkload, SimWorkload by SimFlopsWorkload(1000) {
            override suspend fun invoke() {
            }
        })
        val mapper = object : MSWorkloadMapper {
            override fun createWorkload(msInstance: MSInstance): MSWorkload {
                return workload
            }
        }
        val requestGenerator = RouterRequestGeneratorImpl(
            ProbRouting(listOf(0.62,0.18,0.08,0.12),1),
            LogNormalExe(-4.13, 3.48),
            ProbDepthPolicy(mapOf(0 to 0.5, 2 to 0.5))
        )
        val state = SimulatorState(msConfig,
            requestGenerator,
            RoundRobinLoadBalancer(), FirstComeFirstServe(), FullRequestExe(),
            clock, this, machineModel,
            mapper, (1000*3600*24).toLong(), PoissonDelay(1066.0), 4000
        )

        state.run()
        assert(true)

    }

}
