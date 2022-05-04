package org.opendc.microservice.simulator.microservice

import kotlinx.coroutines.CoroutineScope
import org.opendc.microservice.simulator.common.HelperFunctions
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock

public class MicroserviceGenerator(private val clock: Clock,
                                   private val scope: CoroutineScope,
                                   private val model: MachineModel
){

    public fun generate(configs: MutableList<MicroserviceConfiguration>): MutableList<Microservice> {

        val result: MutableList<Microservice> = mutableListOf()

        if(HelperFunctions().hasDuplicates(configs)){

            print("UUID must be unique. Duplicate configurations present")

            return mutableListOf()

        }

        var config: MicroserviceConfiguration

        for(i in 1..configs.size){

            config = configs[i]

            result.add(Microservice(config.getId(), config.getInstances(), clock, scope, model))

        }

        return result

    }

}
