package org.opendc.microservice.simulator.microservice

import io.opentelemetry.api.metrics.Meter
import kotlinx.coroutines.CoroutineScope
import org.opendc.microservice.simulator.common.HelperFunctions
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock

public class MicroserviceGenerator(private val clock: Clock,
                                   private val scope: CoroutineScope,
                                   private val model: MachineModel,
                                   private val meter: Meter
){

    public fun generate(configs: MutableList<MicroserviceConfiguration>): MutableList<Microservice> {

        val result: MutableList<Microservice> = mutableListOf()

        if(HelperFunctions().hasDuplicates(configs.toTypedArray())){

            print("UUID must be unique. Duplicate configurations present")

            return mutableListOf()

        }

        var config: MicroserviceConfiguration

        for(i in 1..configs.size){

            config = configs[i]

            result.add(Microservice(config.getId(), config.getInstances(), clock, scope, model, meter))

        }

        return result

    }

}
