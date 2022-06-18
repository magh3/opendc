package org.opendc.microservice.simulator.execution

import org.opendc.microservice.simulator.microservice.MSInstance
import java.util.*
import kotlin.Comparator

public class SmallestFirst: QueuePolicy {

    override fun getEntry(queue: Queue<MSInstance.InvocationRequest>): Queue<MSInstance.InvocationRequest> {

        var entryList = queue.toMutableList()

        entryList = entryList.sortedWith(deadineCompare) as MutableList<MSInstance.InvocationRequest>

        // entryList.map{println(it.msReq.getExeTime())}

        return ArrayDeque<MSInstance.InvocationRequest>(entryList)

    }

    private val deadineCompare =  Comparator<MSInstance.InvocationRequest> { a, b ->
        when {
            (a.msReq.getExeTime() == b.msReq.getExeTime()) -> 0
            (a.msReq.getExeTime() > b.msReq.getExeTime()) -> 1
            else -> -1
        }
    }

}
