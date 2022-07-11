package org.opendc.microservice.simulator.routerMapping

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

public class RandomRouting(private val nrOfMS: Int): RoutingPolicy {

    override fun getMicroservices(caller: Microservice?, hopsDone: Int, microservices: List<Microservice>): List<Microservice> {
        var callMS = mutableListOf<Microservice>()
        for(i in 1..nrOfMS){
            callMS.add(getRandomMS(microservices))
        }
        // no duplicates
        callMS = callMS.distinct().toMutableList()
        // should not contain self
        callMS.remove(caller)
        return callMS
    }


    private fun getRandomMS(microservices: List<Microservice>): Microservice{
        val nrOfMicroservices: Int = microservices.size
        val randService = Random.nextInt(0, nrOfMicroservices)
        return microservices[randService]
    }

}
