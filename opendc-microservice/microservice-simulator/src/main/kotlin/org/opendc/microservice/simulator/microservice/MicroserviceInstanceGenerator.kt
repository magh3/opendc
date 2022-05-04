package org.opendc.microservice.simulator.microservice

import kotlinx.coroutines.CoroutineScope
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock
import java.util.*
import kotlin.random.Random

public class MicroserviceInstanceGenerator(private val clock: Clock,
                                           private val scope: CoroutineScope,
                                           private val model: MachineModel
){

    public fun generate(nrOfInstances: Int): Array<MicroserviceInstance>{

        var instances: Array<MicroserviceInstance> = arrayOf()

        for(i in 1..nrOfInstances){

            instances += MicroserviceInstance(UUID.randomUUID(), clock, scope, model)

        }

        return instances

    }

}
