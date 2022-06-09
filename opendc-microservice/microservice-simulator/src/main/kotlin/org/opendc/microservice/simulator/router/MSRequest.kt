package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice

class MSRequest(private val ms: Microservice,
                private val exeTime: Long,
                private val meta: Map<String, Any>){


    public fun getMS(): Microservice {
        return ms
    }

}
