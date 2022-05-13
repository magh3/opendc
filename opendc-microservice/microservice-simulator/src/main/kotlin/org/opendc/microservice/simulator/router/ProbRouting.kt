package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

/**
 * check sum of probabilities should be equal to 1.
 * Also check the size of list should be equal to size of microservices.
 */
class ProbRouting(callProb: List<Double>): RoutingPolicy {

    override fun call(microservices: Array<Microservice>): List<Microservice> {

        TODO()

    }


    private fun getProbMS(microservices: Array<Microservice>, normalizedProb: List<Double>){

        val rand = Random.nextDouble(0.001, 1.0)


    }

}
