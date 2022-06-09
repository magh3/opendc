package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice

public interface RouterRequestGenerator {

    public fun request(microservices: List<Microservice>): RouterRequest

}
