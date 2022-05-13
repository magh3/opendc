package org.opendc.microservice.simulator.microservice

import kotlinx.coroutines.CoroutineScope
import mu.KotlinLogging
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock
import java.util.*

public class MSInstanceDeployer {

    private val logger = KotlinLogging.logger {}

    public fun deploy(msId: UUID, uid: UUID, clock: Clock, scope: CoroutineScope, model: MachineModel ){

        val msInstance = MicroserviceInstance(uid, clock, scope, model)

        msInstance.run()

        logger.info { "Deployed instance with id $uid of microservice $msId" }

    }

}
