package org.opendc.microservice.simulator.mapping

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

public class RandomMicroserviceMapper():MicroserviceMapPolicy {

    override fun mapsTo(microservices: Array<Microservice>): Microservice {

        val nrOfMicroservices: Int = microservices.size

        val randService = Random.nextInt(1, nrOfMicroservices+1)

        return microservices[randService]

    }


}
