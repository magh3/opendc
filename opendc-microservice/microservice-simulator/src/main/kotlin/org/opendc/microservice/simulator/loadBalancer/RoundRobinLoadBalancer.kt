package org.opendc.microservice.simulator.loadBalancer

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*

public class RoundRobinLoadBalancer: LoadBalancer {

    private val msInvokeMap: MutableMap<UUID, Queue<UUID>> = mutableMapOf()

    override fun instance(ms: Microservice, registry: MutableSet<MSInstance>): MSInstance {

        if(msInvokeMap.isEmpty()){}

        TODO("Not yet implemented")
    }

    private fun updateMSInvokeMap(registry: MutableSet<MSInstance>){

        var QueueInstances: Queue<UUID>

        var msId: UUID

        for(instance in registry){

            msId = instance.getMSId()

            if(msId in msInvokeMap.keys){

                msInvokeMap[msId] = msInvokeMap[msId]

            }

        }

    }


}
