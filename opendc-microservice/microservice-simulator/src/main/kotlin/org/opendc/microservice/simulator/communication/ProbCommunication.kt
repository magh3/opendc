package org.opendc.microservice.simulator.communication

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

public class ProbCommunication(private val callProb: List<Double>, private val nrOfMS: Int): CommunicationPolicy {

    private val normalizedProbs = normalizeProb(callProb)

    init{

        val sum = callProb.sumOf{it}

        require(sum == 1.0){"Invalid Probabilities."}

    }

    override fun communicateMs(ms: Microservice, hopsDone: Int, microservices: List<Microservice>): List<Microservice> {

        require(microservices.isNotEmpty()){"No microservice found."}

        require(nrOfMS <= microservices.size){"nr of ms requested cannot be " +
            "more than nr of ms present"}

        if(microservices.size == 1) return microservices.toList()

        else{

            val callMS = mutableListOf<Microservice>()

            for(i in 1..nrOfMS){

                val selectedMs = getProbMS(microservices)

                if(selectedMs != ms) callMS.add(selectedMs)

            }

            return callMS

        }

    }


    private fun getProbMS(microservices: List<Microservice>): Microservice {

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
