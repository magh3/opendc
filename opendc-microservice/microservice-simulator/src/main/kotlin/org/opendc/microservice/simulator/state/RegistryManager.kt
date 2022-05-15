package org.opendc.microservice.simulator.state

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.Microservice

public class RegistryManager {

    private val registry = mutableSetOf<MSInstance>()

    public fun registerInstance(msInstance: MSInstance){

        registry.add(msInstance)

    }


    public fun deregisterInstance(msInstance: MSInstance){

        registry.remove(msInstance)

    }


    public fun getInstances(): MutableSet<MSInstance> {

        return registry

    }

}
