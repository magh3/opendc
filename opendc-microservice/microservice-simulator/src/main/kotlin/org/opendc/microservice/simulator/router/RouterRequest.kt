package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice

public class RouterRequest(private val hopsDone: Int,
                           private val hopMSMap: List<Map<MSRequest, List<MSRequest>>>) {

    public fun getCommRequests(hopsDone: Int, callingReq: MSRequest): List<MSRequest> {
        if(hopsDone > hopMSMap.size -1) return listOf()
        return hopMSMap[hopsDone].get(callingReq) ?: listOf()
    }

    /**
     * returns the ms to call at hopDone
     */
    public fun getInitMSRequests(): List<MSRequest> {
        return hopMSMap[0].keys.toList()
    }

    /**
     * returns hopes done
     */
    public fun getHops(): Int {
        return hopsDone
    }

    public fun getHopMSMap(): List<Map<MSRequest, List<MSRequest>>> {
        return hopMSMap
    }

    override fun toString(): String {
        return hopMSMap.toString()
    }

}
