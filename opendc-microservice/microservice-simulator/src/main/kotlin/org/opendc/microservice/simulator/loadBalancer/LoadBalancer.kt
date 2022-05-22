package org.opendc.microservice.simulator.loadBalancer

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.Microservice


public interface LoadBalancer {

    // return instance of microservice
    public fun instance(ms: Microservice, registry: MutableSet<MSInstance>): MSInstance

}
