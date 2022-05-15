package org.opendc.microservice.simulator.state

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.Microservice


public class LoadBalancer {

    // return instance of microservice
    public fun instance(ms: Microservice, registry: MutableSet<MSInstance>): MSInstance {

        for(instance in registry){

            if(ms.getId() == instance.getMSId()) return instance

        }

        // this should not be reached, return random first instance

        println("ERROR load balancer could not found instance")

        return registry.elementAt(0)

    }

}
