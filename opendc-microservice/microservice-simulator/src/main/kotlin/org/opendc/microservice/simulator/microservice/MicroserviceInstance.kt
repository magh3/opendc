package org.opendc.microservice.simulator.microservice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.opendc.simulator.compute.SimBareMetalMachine
import org.opendc.simulator.compute.SimMachine
import org.opendc.simulator.compute.model.MachineModel
import org.opendc.simulator.compute.power.ConstantPowerModel
import org.opendc.simulator.compute.power.SimplePowerDriver
import org.opendc.simulator.flow.FlowEngine
import java.time.Clock
import java.util.*


public class MicroserviceInstance(private val id: UUID,
                                  private val clock: Clock,
                                  private val scope: CoroutineScope,
                                  private val model: MachineModel){

    public fun getIpLocation(): UUID {

        return id

    }

    /**
     * The machine that will execute the workloads.
     */
    public val machine: SimMachine = SimBareMetalMachine(
        FlowEngine(scope.coroutineContext, clock),
        model,
        SimplePowerDriver(ConstantPowerModel(0.0))
    )


    /**
     * The job associated with the lifecycle of the instance.
     */
    private var job: Job? = null

}
