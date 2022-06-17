package org.opendc.microservice.simulator.routerMapping

import mu.KotlinLogging
import org.opendc.microservice.simulator.router.MSRequest
import org.opendc.microservice.simulator.router.RouterRequest
import java.time.Clock

public class RouterHelper {

    private val logger = KotlinLogging.logger {}


    public fun setEqualSlackExeDeadline(request: RouterRequest, sla: Int, clock: Clock){

        // hops start from zero, size start from 1 so we do -1 from size

        val nrOfHops = request.getHopMSMap().size - 1

        // if depth is 2 then there are 3 microservices involved so 3 stages (totalHops+1)

        val stageDeadline = sla/(nrOfHops+1)

        val slackDistribution = mutableListOf<Int>()

        for(i in 0..(nrOfHops)){

            // slack is divided equally

            slackDistribution.add(stageDeadline)

        }

        val deadlines = getDeadlines(slackDistribution, clock)

        logger.debug { "given sla $sla slack distribution is $slackDistribution" }

        // set slack for all hops

        setSlackAllHops(deadlines, request.getHopMSMap())

    }


    public fun setExeBasedDeadline(request: RouterRequest, sla: Int, clock: Clock){

        // get list of max exe time for each hop

        val maxExeOfHops = getMaxAllHops(request.getHopMSMap())

        // logger.debug { "max exe of hops $maxExeOfHops" }

        // distribute slack according to max of hops

        val slackDistribution = maxExeOfHops.map{((it.toDouble()/maxExeOfHops.sum().toDouble())*sla).toInt() }

        logger.debug { "slack distribution is $slackDistribution" }

        // distribution to deadlines

        val deadlines = getDeadlines(slackDistribution, clock)

        // logger.debug { "deadlines are  $deadlines" }

        // set slack for all hops

        setSlackAllHops(deadlines, request.getHopMSMap())

    }


    private fun getDeadlines(slackDistribution: List<Int>, clock: Clock): List<Long> {

        val deadlines = mutableListOf<Long>()

        for(hop in slackDistribution.indices){

            // if hop is 1 deadline is sum of index 0 and 1

            var hopDeadline: Long = clock.millis()

            for(i in 0..hop){

                hopDeadline += slackDistribution[i]

            }

            deadlines.add(hopDeadline)

        }

        return deadlines.toList()

    }


    private fun setSlackAllHops(exeTimesOfHops: List<Long>, requestMap: List<Map<MSRequest, List<MSRequest>>>){

        require(exeTimesOfHops.size == requestMap.size){"ERROR setting exe times"}

        for(i in requestMap.indices){

            setSlackForHop(exeTimesOfHops[i], requestMap[i])

        }

    }


    private fun setSlackForHop(slackTime: Long, hopMap: Map<MSRequest, List<MSRequest>>){

        // get requests at that hop

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
