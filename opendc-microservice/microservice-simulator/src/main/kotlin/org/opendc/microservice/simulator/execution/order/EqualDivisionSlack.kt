package org.opendc.microservice.simulator.execution.order

import mu.KotlinLogging
import org.opendc.microservice.simulator.router.RouterRequest
import java.time.Clock

public class EqualDivisionSlack: EarliestDeadline() {

    private val logger = KotlinLogging.logger {}

    override fun setMeta(request: RouterRequest, sla: Int, clock: Clock) {
        // hops start from zero, size start from 1 so we do -1 from size
        val nrOfHops = request.getHopMSMap().size - 1
        // if depth is 2 then there are 3 microservices involved so 3 stages (totalHops+1)
        val stageDeadline = sla/(nrOfHops+1)
        val slackDistribution = mutableListOf<Int>()
        for(i in 0..(nrOfHops)){
            // slack is divided equally
            slackDistribution.add(stageDeadline)
        }

        logger.debug { "given sla $sla slack distribution is $slackDistribution" }
        val deadlines = getDeadlines(slackDistribution, clock)
        logger.debug { "deadlines are  $deadlines" }
        // set slack for all hops
        setSlackAllHops(deadlines, request.getHopMSMap())
    }

}
