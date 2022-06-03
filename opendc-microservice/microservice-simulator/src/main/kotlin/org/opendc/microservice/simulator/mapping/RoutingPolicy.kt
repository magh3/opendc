package org.opendc.microservice.simulator.mapping

import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.router.Request

/**
 * Not using
 */
public interface RoutingPolicy{

    public fun call(microservices: MutableList<Microservice>): List<Microservice>

}
