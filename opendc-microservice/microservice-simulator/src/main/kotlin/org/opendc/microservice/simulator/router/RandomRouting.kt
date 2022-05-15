package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

/**
 * check nr should not be greater than ms present
 */
public class RandomRouting(): RoutingPolicy {

    override fun call(microservices: MutableList<Microservice>, nrOfMS: Int): List<Microservice> {

        val callMS = mutableListOf<Microservice>()

        for(i in 1..nrOfMS){

            callMS.add(getRandomMS(microservices))

        }

        return callMS

    }


    private fun getRandomMS(microservices: MutableList<Microservice>): Microservice{

        val nrOfMicroservices: Int = microservices.size

        val randService = Random.nextInt(1, nrOfMicroservices+1)

        return microservices[randService]

    }

}
