package org.opendc.microservice.simulator.routerMapping

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

public class ProbRouting(private val callProb: List<Double>, private val nrOfMS: Int): RoutingPolicy {


    init{

        val sum = callProb.sumOf{it}

        require(sum == 1.0){"Invalid Probabilities."}

    }

    override fun getMicroservices(caller: Microservice?, hopsDone: Int, microservices: List<Microservice>): List<Microservice> {

        var leftMicroservices = microservices

        if(caller != null) leftMicroservices = microservices.filter{it != caller}

        require(leftMicroservices.isNotEmpty()){"No microservice found."}

        require(nrOfMS <= leftMicroservices.size){"nr of ms requested cannot be " +
            "more than nr of ms present"}

        if(leftMicroservices.size == 1) return leftMicroservices.toList()

        else{

            val callMS = mutableListOf<Microservice>()

            val leftCallProb = callProb.toMutableList()

            if(caller in microservices) leftCallProb.removeAt(microservices.toMutableList().indexOf(caller))

            val leftMSNormProb = normalizeProb(leftCallProb)

            for(i in 1..nrOfMS){

                val selectedMs = getProbMS(leftMicroservices, leftMSNormProb)

                if(selectedMs != caller) callMS.add(selectedMs)

            }

            return callMS

        }

    }


    private fun getProbMS(leftMicroservices: List<Microservice>, leftMSNormProb: List<Double>): Microservice {

        val randProb = Random.nextDouble(0.001, 1.0)

        // there are at least 2 ms, so at least 2 probs

        var currentProb: Double

        var nextProb: Double

        for(i in 0..(leftMSNormProb.size - 2) ){

            // -2 because last index is at -1 and next prob end at -2

            currentProb = leftMSNormProb[i]

            nextProb = leftMSNormProb[i+1]

            if(randProb >= currentProb && randProb < nextProb) return leftMicroservices[i]

            else if(i == leftMSNormProb.size - 2) {

                // Last probability in list matches so return last instance

                return leftMicroservices[i+1]

            }

        }

        // this should not be reached

        print("ERROR getting MS from probability. Returning first MS")

        return leftMicroservices[0]

    }


    // normalizedProb are the start values of cumulative prob
    private fun normalizeProb(callProb: List<Double>): MutableList<Double> {

        // list of end of each prob
        val normalizedProbs = mutableListOf<Double>()

        val sum = callProb.sumByDouble { it }

        var partialSum = 0.0

        for(i in callProb.indices){

            normalizedProbs.add(partialSum)

            partialSum += callProb[i] / sum


        }

        return normalizedProbs

    }

}
