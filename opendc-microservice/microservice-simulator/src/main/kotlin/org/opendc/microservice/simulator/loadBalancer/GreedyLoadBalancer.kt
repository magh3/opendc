package org.opendc.microservice.simulator.loadBalancer

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*

/**
 * forward requests to least loaded instance.
 * Load is calculated from exe time
 */
public class GreedyLoadBalancer: LoadBalancer {

    override fun instance(ms: Microservice, registry: MutableSet<MSInstance>): MSInstance {

        val msInstances = filterMSInstances(ms, registry)

        // get load for instances of the specific ms

        val instanceLoadMap = getLoads(msInstances)

        // find min instance load entry

        var min: Map.Entry<UUID, Int> = instanceLoadMap.iterator().next();

        for (entry in instanceLoadMap) {

            if ( min.value > entry.value) {

                min = entry

            }
        }

        return instanceFromId(min.key, registry)

    }


    private fun getLoads(msInstances: MutableSet<MSInstance>): MutableMap<UUID, Int> {

        val instanceLoadMap: MutableMap<UUID, Int> = mutableMapOf()

        for(instance in msInstances){

            instanceLoadMap[instance.getId()] = instance.load()

        }

        return instanceLoadMap

    }


}
