package org.opendc.microservice.simulator.loadBalancer

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.Microservice

/**
 * Useless mock load balancer.
 * picks the first instance in the list.
 */
public class MockLoadBalancer: LoadBalancer {

    // return instance of microservice
    override public fun instance(ms: Microservice, registry: Set<MSInstance>): MSInstance {
        for(instance in registry){
            if(ms.getId() == instance.getMSId()) return instance
        }

        // this should not be reached, return random first instance
        require(true){"ERROR load balancer could not found instance. Returning first random instance"}
        return registry.elementAt(0)
    }

}
