package org.opendc.microservice.simulator.router

import mu.KotlinLogging
import org.opendc.microservice.simulator.routerMapping.RoutingPolicy
import org.opendc.microservice.simulator.execution.ExeDelay
import org.opendc.microservice.simulator.microservice.Microservice
import java.time.Clock

public class RouterRequestGeneratorImpl(private val routingPolicy: RoutingPolicy,
                                        private val exePolicy: ExeDelay,
                                        private val depthPolicy: DepthPolicy): RouterRequestGenerator {

    private val logger = KotlinLogging.logger {}

    /**
     * only make the request, not add meta
     */
    override fun request(microservices: List<Microservice>): RouterRequest {
        val hopsList = mutableListOf<Map<MSRequest, List<MSRequest>>>()
        val initialMicroservices = routingPolicy.getMicroservices(null, 0, microservices)
        var callingRequests = initialMicroservices.map{
            val exeTime = exePolicy.time(it, 0)
            val metaMap = mutableMapOf<String, Any>()
            MSRequest(it, exeTime, metaMap)
        }

        val reqDepth = depthPolicy.getDepth()
        logger.debug{"making request with depth $reqDepth"}
        // runs at least once for 0.
        for(currentDepth in 0..reqDepth){
            // comm for depth i list
            val msCommMap = mutableMapOf<MSRequest, List<MSRequest>>()
            val commRequestsAtHop = mutableListOf<MSRequest>()
            for(msReq in callingRequests){
                val ms = msReq.getMS()
                val commMSReqs = mutableListOf<MSRequest>()
                if(currentDepth < reqDepth){

                    // depth is not last so can still communicate
                    // fill comm ms
                    val commMicroservices = routingPolicy.getMicroservices(ms, currentDepth, microservices)
                    for(commMS in commMicroservices){
                        // hope is done when comm ms executes
                        logger.debug{"adding comm ms: $commMS"}
                        val hopDone = currentDepth+1
                        val commExeTime = exePolicy.time(ms, hopDone)
                        val commMeta = mutableMapOf<String, Any>()
                        val commReq = MSRequest(commMS, commExeTime, commMeta)
                        // map entry, communication at the hop of the specific ms
                        commMSReqs.add(commReq)
                        // all communication requests in the hop
                        commRequestsAtHop.add(commReq)
                    }
                }
                // add map entry, empty communication request list for last depth
                msCommMap[msReq] = commMSReqs
            }
            hopsList.add(msCommMap)
            // map after hop zero can have only ms that were previously communicated to
            callingRequests = commRequestsAtHop
        }
        return RouterRequest(0,hopsList.toList())
    }

}
