package org.opendc.microservice.simulator.mapping

import org.opendc.microservice.simulator.microservice.Microservice

/**
 * Not using
 */
public interface RoutingPolicy{

    public fun call(microservices: MutableList<Microservice>, nrOfMS: Int = 1): List<Microservice>

}
