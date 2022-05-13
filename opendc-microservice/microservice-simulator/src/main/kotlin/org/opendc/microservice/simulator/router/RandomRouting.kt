package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

/**
 * check nr should not be greater than ms present
 */
class RandomRouting(private var nrOfMS: Int = 1): RoutingPolicy {

    override fun call(microservices: Array<Microservice>): List<Microservice> {

        val callMS = mutableListOf<Microservice>()

        for(i in 1..nrOfMS){

            callMS.add(getRandomMS(microservices))

        }

        return callMS

    }


    private fun getRandomMS(microservices: Array<Microservice>): Microservice{

        val nrOfMicroservices: Int = microservices.size

        val randService = Random.nextInt(1, nrOfMicroservices+1)

        return microservices[randService]

    }

}
