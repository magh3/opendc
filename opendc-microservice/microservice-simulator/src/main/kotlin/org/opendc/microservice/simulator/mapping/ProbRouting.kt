package org.opendc.microservice.simulator.mapping

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

/**
 * check sum of probabilities should be equal to 1.
 * Also check the size of list should be equal to size of microservices.
 */
public class ProbRouting(private val callProb: List<Double>, private val nrOfMS: Int): RoutingPolicy {

    private val normalizedProbs = normalizeProb(callProb)

    init{

        val sum = callProb.sumOf{it}

        require(sum == 1.0){"Invalid Probabilities."}

    }

    override fun call(microservices: MutableList<Microservice>): List<Microservice> {

        require(microservices.isNotEmpty()){"No microservice found."}

        require(nrOfMS <= microservices.size){"nr of ms requested cannot be " +
            "more than nr of ms present"}

        if(microservices.size == 1) return microservices.toList()

        else{

            val callMS = mutableListOf<Microservice>()

            for(i in 1..nrOfMS){

                callMS.add(getProbMS(microservices))

            }

            return callMS

        }

    }


    private fun getProbMS(microservices: MutableList<Microservice>): Microservice {

        val randProb = Random.nextDouble(0.001, 1.0)

        // there are at least 2 ms, so at least 2 probs

        var currentProb: Double

        var nextProb: Double

        for(i in 0..(normalizedProbs.size - 2) ){

            // -2 because last index is at -1 and next prob end at -2

            currentProb = normalizedProbs[i]

            nextProb = normalizedProbs[i+1]

            if(randProb >= currentProb && randProb < nextProb) return microservices[i]

            else if(i == normalizedProbs.size - 2) {

                // Last probability in list matches so return last instance

                return microservices[i+1]

            }

        }

        // this should not be reached

        print("ERROR getting MS from probability. Returning first MS")

        return microservices[0]

    }


    // normalizedProb are the start values of cumulative prob
    private fun normalizeProb(callProb: List<Double>): MutableList<Double> {

        // list of end of each prob
        val normalizedProbs = mutableListOf<Double>()

        var partialSum = 0.0

        for(i in callProb.indices){

            normalizedProbs.add(partialSum)

            partialSum += callProb[i]


        }

        return normalizedProbs

    }

}
