package org.opendc.microservice.simulator.execution

import mu.KotlinLogging
import org.opendc.microservice.simulator.router.MSRequest
import org.opendc.microservice.simulator.router.RouterRequest
import java.time.Clock

public class EXDivisionSlack: EarliestDeadlineNoExe() {

    private val logger = KotlinLogging.logger {}

    override fun setMeta(request: RouterRequest, sla: Int, clock: Clock) {

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


    fun getMaxAllHops(requestMap: List<Map<MSRequest, List<MSRequest>>>): List<Long> {

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
