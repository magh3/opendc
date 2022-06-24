package org.opendc.microservice.simulator.loadBalancer

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*


public interface LoadBalancer {

    // return instance of microservice
    public fun instance(ms: Microservice, registry: Set<MSInstance>): MSInstance

    public fun instanceFromId(id: UUID, instances: Set<MSInstance>): MSInstance {

        for(instance in instances){

            if(instance.getId() == id) return instance

        }

        println("ERROR. No instance match with given id. returning random first instance")

        return instances.elementAt(0)

    }


    public fun filterMSInstances(ms: Microservice, registry: Set<MSInstance>): MutableSet<MSInstance> {

        val msInstances: MutableSet<MSInstance> = mutableSetOf()

        val msId = ms.getId()

        for(instance in registry){

            if(msId == instance.getMSId()) msInstances.add(instance)

        }

        return msInstances

    }




}
