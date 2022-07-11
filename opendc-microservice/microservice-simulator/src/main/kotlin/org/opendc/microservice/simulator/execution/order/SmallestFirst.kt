package org.opendc.microservice.simulator.execution.order

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.router.RouterRequest
import java.time.Clock
import java.util.*
import kotlin.Comparator

public class SmallestFirst: QueuePolicy {

    override fun getEntry(queue: Queue<MSInstance.InvocationRequest>): Queue<MSInstance.InvocationRequest> {
        var entryList = queue.toMutableList()
        entryList = entryList.sortedWith(deadineCompare) as MutableList<MSInstance.InvocationRequest>
        return ArrayDeque<MSInstance.InvocationRequest>(entryList)
    }

    override fun setMeta(request: RouterRequest, sla: Int, clock: Clock) {}

    private val deadineCompare =  Comparator<MSInstance.InvocationRequest> { a, b ->
        when {
            (a.msReq.getExeTime() == b.msReq.getExeTime()) -> 0
            (a.msReq.getExeTime() > b.msReq.getExeTime()) -> 1
            else -> -1
        }
    }

}
