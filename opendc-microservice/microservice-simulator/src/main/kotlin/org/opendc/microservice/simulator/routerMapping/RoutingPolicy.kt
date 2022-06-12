package org.opendc.microservice.simulator.routerMapping

import org.opendc.microservice.simulator.microservice.Microservice

public interface RoutingPolicy{

    /**
     * Not using right now
     */

    public fun getMicroservices(caller: Microservice?, hopsDone: Int, microservices: List<Microservice>):List<Microservice>

}
