package org.opendc.microservice.simulator.router

import kotlin.random.Random
import kotlin.time.seconds

public class ProbDepthPolicy(private val depthProb: Map<Int, Double>): DepthPolicy {


    init{

        val sum = depthProb.values.sumOf{it}

        require(sum == 1.0){"Invalid Probabilities."}

    }


    override fun getDepth(): Int {

        if(depthProb.size == 1) return depthProb.keys.toList()[0]

        val randProb = Random.nextDouble(0.001, 1.0)

        // there are at least 2 keys in map, so at least 2 probs

        var currentProb: Double

        var nextProb: Double

        val mapEntries = depthProb.toList()

        val normalizedProbs = normalizeProb(mapEntries.map{it.second})

        for(i in 0..(normalizedProbs.size - 2) ){

            // -2 because last index is at -1 and next prob end at -2

            currentProb = normalizedProbs[i]

            nextProb = normalizedProbs[i+1]

            if(randProb >= currentProb && randProb < nextProb) return mapEntries[i].first

            else if(i == normalizedProbs.size - 2) {

                // Last probability in list matches so return last instance

                return mapEntries[i+1].first

            }

        }

        // this should not be reached

        print("ERROR getting MS from probability. Returning first MS")

        return mapEntries[0].first

    }


    // get a sample e.g 040. go through map. making partial sum. this and next key check return current key.


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
