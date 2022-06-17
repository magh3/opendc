package org.opendc.microservice.simulator.execution

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.router.MSRequest
import java.util.*

public class EarliestDeadlineNoExe: QueuePolicy {

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

}
