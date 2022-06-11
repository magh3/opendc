package org.opendc.microservice.simulator.execution

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.router.MSRequest
import java.util.*

public class EarliestDeadlineNoExe: QueuePolicy {

    override fun getEntry(queue: Queue<MSInstance.InvocationRequest>): Queue<MSInstance.InvocationRequest> {

        var entryList = queue.toMutableList()

        entryList = entryList.sortedWith(deadineCompare.reversed()) as MutableList<MSInstance.InvocationRequest>

        return ArrayDeque<MSInstance.InvocationRequest>(entryList)

    }

    private val deadineCompare =  Comparator<MSInstance.InvocationRequest> { a, b ->
        when {
            (a.msReq.getMeta()["stageDeadline"].toString().toInt() == b.msReq.getMeta()["stageDeadline"].toString().toInt()) -> 0
            (a.msReq.getMeta()["stageDeadline"].toString().toInt() > b.msReq.getMeta()["stageDeadline"].toString().toInt()) -> 1
            else -> -1
        }
    }

}
