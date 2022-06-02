package org.opendc.microservice.simulator.mapping

import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.router.Request
import org.opendc.microservice.simulator.router.RequestV2
import kotlin.random.Random

/**
 * check nr should not be greater than ms present
 */
public class RandomRouting(private val nrOfMS: Int): RoutingPolicy {

    override fun call(microservices: MutableList<Microservice>): List<Microservice> {

        val callMS = mutableListOf<Microservice>()

        for(i in 1..nrOfMS){

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

    override fun callV2(microservices: MutableList<Microservice>): List<RequestV2> {

        val callMS = mutableListOf<RequestV2>()

        for(i in 1..nrOfMS){

            callMS.add(RequestV2(getRandomMS(microservices), 0) )

        }

        return callMS

    }


    private fun getRandomMS(microservices: MutableList<Microservice>): Microservice{

        val nrOfMicroservices: Int = microservices.size

        val randService = Random.nextInt(0, nrOfMicroservices)

        return microservices[randService]

    }

}
