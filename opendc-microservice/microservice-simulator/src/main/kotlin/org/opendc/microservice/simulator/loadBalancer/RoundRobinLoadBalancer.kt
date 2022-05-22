package org.opendc.microservice.simulator.loadBalancer

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*


public class RoundRobinLoadBalancer: LoadBalancer {

    private val msInvokeMap: MutableMap<UUID, Queue<UUID>> = mutableMapOf()

    override fun instance(ms: Microservice, registry: MutableSet<MSInstance>): MSInstance {

        updateDeletedInstances(registry)

        updateNewInstances(registry)

        val selectedInstanceId = selectInstance(ms)

        for(instance in registry){

            if(instance.getId() == selectedInstanceId) return instance

        }

        println("ERROR selecting instance. returning random first instance")

        return registry.elementAt(0)

    }


    private fun selectInstance(ms: Microservice): UUID {

        val msInstances = msInvokeMap[ms.getId()] ?: LinkedList(listOf(UUID.randomUUID()))

        if(msInstances.isEmpty()) println("ERROR. No Instance found for this microservice")

        return msInstances.remove()

    }


    private fun updateNewInstances(registry: MutableSet<MSInstance>){

        var queueInstances: Queue<UUID>

        var msId: UUID

        for(instance in registry){

            msId = instance.getMSId()

            if(msId !in msInvokeMap.keys){

                // ms not present in map
                // add ms and init with empty queue

                msInvokeMap[msId] = LinkedList<UUID>(listOf())

            }

            // msId is present

            // get pre queue

            queueInstances = msInvokeMap[msId] ?: LinkedList<UUID>(listOf())

            // check if instance is present in queue

            if(instance.getId() !in queueInstances){

                queueInstances.add(instance.getId())

                msInvokeMap[msId] = queueInstances

            }


        }

    }


    private fun updateDeletedInstances(registry: MutableSet<MSInstance>){

        val registryInstanceIds : MutableList<UUID> = mutableListOf()

        // make list of all instance ids

        for(instance in registry){

            registryInstanceIds.add(instance.getId())

        }

        var newIds: MutableList<UUID>

        for((k,v) in msInvokeMap){

            for(instanceId in v){

                if(instanceId !in registryInstanceIds){

                    newIds = v.toMutableList()

                    newIds.remove(instanceId)

                    msInvokeMap[k] = LinkedList<UUID>(newIds)

                }

            }

        }

    }

}
