package org.opendc.microservice.simulator.loadBalancer

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*

/**
 * forward request to instance with least number of waiting requests.
 */
public class LeastConnectionLoadBalancer: LoadBalancer {

    override fun instance(ms: Microservice, registry: MutableSet<MSInstance>): MSInstance {

        val msInstances = filterMSInstances(ms, registry)

        // get load for instances of the specific ms

        val instanceConnectionsMap = getConnections(msInstances)

        // find min instance load entry

        var min: Map.Entry<UUID, Int> = instanceConnectionsMap.iterator().next();

        for (entry in instanceConnectionsMap) {

            if ( min.value > entry.value) {

                min = entry

            }
        }

        return instanceFromId(min.key, registry)

    }


    private fun getConnections(msInstances: MutableSet<MSInstance>): MutableMap<UUID, Int> {

        val instanceConnectionsMap: MutableMap<UUID, Int> = mutableMapOf()

        for(instance in msInstances){

            instanceConnectionsMap[instance.getId()] = instance.connections()

        }

        return instanceConnectionsMap

    }
}
