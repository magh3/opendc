package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice

public class RequestV2(private val ms: Microservice,
                private val hops: Int) {

    public fun getHops(): Int {

        return hops

    }

    public fun ms(): Microservice {

        return ms

    }

}
