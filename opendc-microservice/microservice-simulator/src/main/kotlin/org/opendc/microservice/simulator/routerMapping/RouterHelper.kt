package org.opendc.microservice.simulator.routerMapping

import mu.KotlinLogging
import org.opendc.microservice.simulator.router.MSRequest
import org.opendc.microservice.simulator.router.RouterRequest

public class RouterHelper {

    private val logger = KotlinLogging.logger {}

    public fun setExeBasedDeadline(request: RouterRequest, sla: Int){

        // get list of max exe time for each hop

        val maxExeOfHops = getMaxAllHops(request.getHopMSMap())

        // distribute slack according to max of hops

        val slackDistribution = maxExeOfHops.map{((it/maxExeOfHops.sum())*sla).toInt() }

        logger.debug { "given sla $sla slack distribution is $slackDistribution" }

        // set slack for all hops

        setSlackAllHops(slackDistribution, request.getHopMSMap())

    }


    private fun setSlackAllHops(exeTimesOfHops: List<Int>, requestMap: List<Map<MSRequest, List<MSRequest>>>){

        require(exeTimesOfHops.size == requestMap.size){"ERROR setting exe times"}

        for(i in requestMap.indices){

            setSlackForHop(exeTimesOfHops[i].toLong(), requestMap[i])

        }

    }


    private fun setSlackForHop(slackTime: Long, hopMap: Map<MSRequest, List<MSRequest>>){

        // get reqs at that hop

        val msRequests = hopMap.keys

        for(msReq in msRequests){

            msReq.setMeta("stageDeadline", slackTime)

        }

    }


    private fun getMaxAllHops(requestMap: List<Map<MSRequest, List<MSRequest>>>): List<Long> {

        val maxExeOfHops = mutableListOf<Long>()

        for(currentHop in requestMap.indices){

            // get max of currentHop

            maxExeOfHops.add(maxExetimeOfHop(currentHop, requestMap)?: 0)

        }

        return maxExeOfHops.toList()

    }


    private fun maxExetimeOfHop(currentHop: Int, requestMap: List<Map<MSRequest, List<MSRequest>>>): Long? {

        // get map at currentHop

        val currentHopMap = requestMap[currentHop]

        //  get requests in current hop

        val currentHopReqs = currentHopMap.keys

        // get list of exe times

        val exeTimesOfCurrentHop = currentHopReqs.map{it.getExeTime()}

        return exeTimesOfCurrentHop.maxOrNull()

    }

}
