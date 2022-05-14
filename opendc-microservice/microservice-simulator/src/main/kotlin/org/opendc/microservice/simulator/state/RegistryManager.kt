package org.opendc.microservice.simulator.state

import org.opendc.microservice.simulator.microservice.MicroserviceInstance

class RegistryManager {

    private val registry = mutableSetOf<MicroserviceInstance>()

    public fun registerInstance(msInstance: MicroserviceInstance){

        registry.add(msInstance)

    }


    public fun deregisterInstance(msInstance: MicroserviceInstance){

        registry.remove(msInstance)

    }

}
