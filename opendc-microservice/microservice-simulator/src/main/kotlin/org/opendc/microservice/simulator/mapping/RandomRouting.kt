package org.opendc.microservice.simulator.mapping

import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.router.Request
import kotlin.random.Random

/**
 * check nr should not be greater than ms present
 */
public class RandomRouting(private val nrOfMS: Int): RoutingPolicy {

    override fun call(microservices: MutableList<Microservice>): List<Microservice> {

        val callMS = mutableListOf<Microservice>()

        for(i in 1..2){

            callMS.add(getRandomMS(microservices))

        }

        return callMS

    }


    override fun invokeOrder(microservices: MutableList<Microservice>): List<Request> {

        val invokeRequests = mutableListOf<Request>()

        for(i in 1..nrOfMS){

            invokeRequests.add(Request(call(microservices).distinct()))

        }

        return invokeRequests
    }


    private fun getRandomMS(microservices: MutableList<Microservice>): Microservice{

        val nrOfMicroservices: Int = microservices.size

        val randService = Random.nextInt(0, nrOfMicroservices)

        return microservices[randService]

    }

}
