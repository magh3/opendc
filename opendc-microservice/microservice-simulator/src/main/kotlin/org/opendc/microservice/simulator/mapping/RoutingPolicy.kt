package org.opendc.microservice.simulator.mapping

import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.router.Request
import org.opendc.microservice.simulator.router.RequestV2

/**
 * Not using
 */
public interface RoutingPolicy{

    public fun call(microservices: MutableList<Microservice>): List<Microservice>

    public fun invokeOrder(microservices: MutableList<Microservice>): List<Request>

    public fun callV2(microservices: MutableList<Microservice>): List<RequestV2>

}
