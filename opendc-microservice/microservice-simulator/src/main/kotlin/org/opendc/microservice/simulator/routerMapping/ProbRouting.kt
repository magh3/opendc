package org.opendc.microservice.simulator.routerMapping

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

public class ProbRouting(private val callProb: List<Double>, private val nrOfMS: Int): RoutingPolicy {

    init{
        val sum = callProb.sumOf{it}
        require(sum == 1.0){"Invalid Probabilities."}

    }

    override fun getMicroservices(caller: Microservice?, hopsDone: Int, microservices: List<Microservice>): List<Microservice> {
        val leftCallProb = callProb.toMutableList()
        if(caller != null && caller in microservices) leftCallProb.removeAt(microservices.toMutableList().indexOf(caller))
        var leftMicroservices = microservices
        if(caller != null && caller in microservices) leftMicroservices = microservices.filter{it != caller}
        require(leftMicroservices.isNotEmpty()){"No microservice found."}

        val callMS = mutableListOf<Microservice>()
        val leftMSNormProb = normalizeProb(leftCallProb)
        for(i in 1..nrOfMS){
            val selectedMs = getProbMS(leftMicroservices, leftMSNormProb)
            if(selectedMs != caller) callMS.add(selectedMs)
        }
        return callMS
    }

    private val randGene = Random(0)

    private fun getProbMS(leftMicroservices: List<Microservice>, leftMSNormProb: List<Double>): Microservice {
        require(leftMSNormProb.size == leftMicroservices.size)
            {"ms ${leftMicroservices.size} and prob size ${leftMSNormProb.size} not equal"}
        // there are at least 2 ms, so at least 2 probs
        var currentProb: Double
        var nextProb: Double

        for(i in 0..(leftMSNormProb.size - 2) ){
            // -2 because last index is at -1 and next prob end at -2
            currentProb = leftMSNormProb[i]
            nextProb = leftMSNormProb[i+1]
            val randProb = randGene.nextDouble(0.001, 1.0)

            if(randProb >= currentProb && randProb < nextProb) return leftMicroservices[i]
            else if(i == leftMSNormProb.size - 2) {
                // Last probability in list matches so return last instance
                return leftMicroservices[i+1]
            }
        }
        // this should not be reached
        require(false){"ERROR getting MS from probability. Returning first MS"}
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
