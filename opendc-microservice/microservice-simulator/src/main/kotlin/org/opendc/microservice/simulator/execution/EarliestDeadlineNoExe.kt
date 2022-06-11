package org.opendc.microservice.simulator.execution

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.router.MSRequest
import java.util.*

public class EarliestDeadlineNoExe: QueuePolicy {

    override fun getEntry(queue: Queue<MSInstance.InvocationRequest>): Queue<MSInstance.InvocationRequest> {

        val entryList = queue.toMutableList()

        entryList.sortedWith(deadineCompare)

        return ArrayDeque<MSInstance.InvocationRequest>(entryList)

    }

    private val deadineCompare =  Comparator<MSInstance.InvocationRequest> { a, b ->
        when {
            (a.msReq.getMeta()["stageDeadline"].toString().toInt() < b.msReq.getMeta()["stageDeadline"].toString().toInt()) -> 0
            else -> 1
        }
    }

}
