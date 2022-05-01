package org.opendc.microservice.simulator.mapping

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

class RandomMicroserviceMapper(private val microservices: Array<Microservice>):MicroserviceMapPolicy {

    override fun mapsTo(): Microservice {

        val nrOfMicroservices: Int = microservices.size

        val randService = Random.nextInt(1, nrOfMicroservices+1)

        return microservices[randService]

    }


}
