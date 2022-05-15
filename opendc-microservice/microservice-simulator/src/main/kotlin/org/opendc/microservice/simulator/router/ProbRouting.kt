package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

/**
 * check sum of probabilities should be equal to 1.
 * Also check the size of list should be equal to size of microservices.
 */
public class ProbRouting(callProb: List<Double>): RoutingPolicy {

    private val normalizedProbs = normalizeProb(callProb)

    init{

        val sum = callProb.sumOf{it}

        require(sum == 1.0){"Invalid Probabilities."}

    }

    override fun call(microservices: MutableList<Microservice>, nrOfMS: Int): List<Microservice> {

        require(microservices.isNotEmpty()){"No microservice found."}

        if(microservices.size == 1) return microservices.toList()

        else{

            return mutableListOf( getProbMS(microservices) )

        }

    }


    private fun getProbMS(microservices: MutableList<Microservice>): Microservice {

        val randProb = Random.nextDouble(0.001, 1.0)

        // there are at least 2 ms, so at least 2 probs

        var currentProb: Double

        var nextProb: Double

        for(i in normalizedProbs.indices){

            currentProb = normalizedProbs[i]

            nextProb = normalizedProbs[i+1]

            if(randProb >= currentProb && randProb < nextProb) return microservices[i]

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
