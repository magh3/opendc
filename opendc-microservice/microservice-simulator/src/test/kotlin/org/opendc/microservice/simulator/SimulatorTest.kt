package org.opendc.microservice.simulator

import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opendc.simulator.compute.model.MachineModel
import org.opendc.simulator.compute.model.MemoryUnit
import org.opendc.simulator.compute.model.ProcessingNode
import org.opendc.simulator.compute.model.ProcessingUnit
import org.opendc.simulator.core.runBlockingSimulation
import java.util.*
import org.opendc.compute.workload.telemetry.SdkTelemetryManager
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import org.opendc.compute.workload.topology.HostSpec
import org.opendc.microservice.simulator.execution.*
import org.opendc.microservice.simulator.execution.order.EqualDivisionSlack
import org.opendc.microservice.simulator.execution.order.FirstComeFirstServe
import org.opendc.microservice.simulator.routerMapping.ProbRouting
import org.opendc.microservice.simulator.router.PoissonDelay
import org.opendc.microservice.simulator.loadBalancer.*
import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.MSConfigGenerator
import org.opendc.microservice.simulator.router.ProbDepthPolicy
import org.opendc.microservice.simulator.router.RouterRequestGeneratorImpl
import org.opendc.microservice.simulator.state.SimulatorState
import org.opendc.microservice.simulator.trace.MSTraceReader
import org.opendc.microservice.simulator.workload.MSWorkload
import org.opendc.microservice.simulator.workload.MSWorkloadMapper
import org.opendc.simulator.compute.kernel.SimSpaceSharedHypervisorProvider
import org.opendc.simulator.compute.power.ConstantPowerModel
import org.opendc.simulator.compute.power.SimplePowerDriver
import org.opendc.simulator.compute.workload.SimFlopsWorkload
import org.opendc.simulator.compute.workload.SimWorkload
import java.io.File


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

        // experiment 1 setup ms config: 4,listOf(4,1,1,2)

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


    @Test
    fun readFile(){

        val trace = MSTraceReader().parse(File("src/main/resources/azure_2021.txt"))

        println(trace[0])

    }


    @Test
    fun exeTimes(){

        //val time = LogNormalExe(-0.38, 2.36).time(Microservice(UUID.randomUUID()),0)

        //println(time)

    }


    @Test
    fun msConstructTest() = runBlockingSimulation {

        val telemetry = SdkTelemetryManager(clock)
        // val meterProvider = MeterProvider.noop().get("Test 1")
        // val meterProvider = telemetry.createMeterProvider(createHostSpec(1))
        val meterProvider = SdkMeterProvider.builder()

        //val microservice = Microservice(UUID.randomUUID())

        // println(microservice.getId())

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

}
