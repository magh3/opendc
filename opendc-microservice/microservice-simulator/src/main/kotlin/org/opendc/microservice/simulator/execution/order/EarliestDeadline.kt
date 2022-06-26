package org.opendc.microservice.simulator.execution.order

import mu.KotlinLogging
import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.router.MSRequest
import org.opendc.microservice.simulator.router.RouterRequest
import java.time.Clock
import java.util.*

public open class EarliestDeadline: QueuePolicy {

    private val logger = KotlinLogging.logger {}

    override fun getEntry(queue: Queue<MSInstance.InvocationRequest>): Queue<MSInstance.InvocationRequest> {

        var entryList = queue.toMutableList()

        entryList = entryList.sortedWith(deadineCompare) as MutableList<MSInstance.InvocationRequest>

        // entryList.map{println(it.msReq.getMeta()["stageDeadline"])}

        return ArrayDeque<MSInstance.InvocationRequest>(entryList)

    }

    private val deadineCompare =  Comparator<MSInstance.InvocationRequest> { a, b ->
        when {
            (a.msReq.getMeta()["stageDeadline"] as Long == b.msReq.getMeta()["stageDeadline"] as Long) -> 0
            (a.msReq.getMeta()["stageDeadline"] as Long > b.msReq.getMeta()["stageDeadline"] as Long) -> 1
            else -> -1
        }
    }


    override fun setMeta(request: RouterRequest, sla: Int, clock: Clock) {

    }


    public fun getDeadlines(slackDistribution: List<Int>, clock: Clock): List<Long> {

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


    public fun setSlackAllHops(exeTimesOfHops: List<Long>, requestMap: List<Map<MSRequest, List<MSRequest>>>){

        require(exeTimesOfHops.size == requestMap.size){"ERROR setting exe times"}

        for(i in requestMap.indices){

            setSlackForHop(exeTimesOfHops[i], requestMap[i])

        }

    }


    private fun setSlackForHop(slackTime: Long, hopMap: Map<MSRequest, List<MSRequest>>){

        // get requests at that hop

        // if hop is zero, reqs are only the keys.

        // if hop is greater than zero then reqs are (hop - 1)

        val msRequests = hopMap.keys

        for(msReq in msRequests){

            msReq.setMeta("stageDeadline", slackTime)

        }

    }

}
