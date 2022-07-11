package org.opendc.microservice.simulator.state

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*

public class RegistryManager(){

    private val microservices: MutableList<Microservice> = mutableListOf()
    private val registry = mutableSetOf<MSInstance>()

    public fun registerInstance(msInstance: MSInstance){
        registry.add(msInstance)
    }

    public suspend fun deregisterInstance(msInstance: MSInstance){
        msInstance.close()
        registry.remove(msInstance)
    }

    public fun getInstances(): Set<MSInstance> {
        return registry
    }

    public fun getInstances(ms: Microservice): List<MSInstance> {
        return registry.filter{it.getMSId() == ms.getId()}
    }

    public fun getMicroservices(): MutableList<Microservice> {
        return microservices
    }

    public fun addMs(ms: Microservice){
        microservices.add(ms)
    }

    public fun msFromID(msId: UUID): Microservice? {
        for(ms in microservices){
            if(ms.getId() == msId) return ms
        }
        return null
    }

}
