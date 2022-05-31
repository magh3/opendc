package org.opendc.microservice.simulator.microservice

import kotlinx.coroutines.CoroutineScope
import mu.KotlinLogging
import org.opendc.microservice.simulator.state.RegistryManager
import org.opendc.microservice.simulator.state.SimulatorState
import org.opendc.microservice.simulator.workload.MSWorkloadMapper
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock
import java.util.*

public class MSInstanceDeployer {

    private val logger = KotlinLogging.logger {}

    public fun deploy(msId: UUID, uid: UUID, simState: SimulatorState, clock: Clock, scope: CoroutineScope,
                      model: MachineModel, registryManager: RegistryManager,
                      mapper: MSWorkloadMapper ): MSInstance {

        val msInstance = MSInstance(msId, uid, simState, clock, scope, model, registryManager, mapper)

        msInstance.run()

        // logger.info { "Deployed instance with id $uid of microservice $msId" }
        println("Deployed instance with id $uid of microservice $msId")

        return msInstance

    }

}
