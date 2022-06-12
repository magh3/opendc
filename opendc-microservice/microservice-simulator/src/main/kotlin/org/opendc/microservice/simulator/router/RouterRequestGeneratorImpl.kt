package org.opendc.microservice.simulator.router

import mu.KotlinLogging
import org.opendc.microservice.simulator.routerMapping.RoutingPolicy
import org.opendc.microservice.simulator.execution.ExeDelay
import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.random.Random

public class RouterRequestGeneratorImpl(private val routingPolicy: RoutingPolicy,
                                        private val exePolicy: ExeDelay,
                                        private val depth: Int = 1): RouterRequestGenerator {

    private val logger = KotlinLogging.logger {}


    override fun request(microservices: List<Microservice>): RouterRequest {

        val hopsList = mutableListOf<Map<MSRequest, List<MSRequest>>>()

        var callingMicroservices = routingPolicy.getMicroservices(null, 0, microservices)

        val reqDepth = Random.nextInt(0,depth+1)

        logger.debug{"making request with depth $reqDepth"}

        // runs at least once for 0.

        for(currentDepth in 0..reqDepth){

            // comm for depth i list

            val msCommMap = mutableMapOf<MSRequest, List<MSRequest>>()

            val commSet = mutableSetOf<Microservice>()

            var maxExe: Long = 0

            logger.debug{"outerMS is $callingMicroservices"}

            for(ms in callingMicroservices){

                // generate comm microservices for each calling microservice

                val exeTime = exePolicy.time(ms, currentDepth)

                if(exeTime > maxExe) maxExe = exeTime

                val metaMap = mutableMapOf<String, Any>()

                val commMSReqs = mutableListOf<MSRequest>()

                if(currentDepth < reqDepth){

                    // routerMapping ms

                    val innerMS = routingPolicy.getMicroservices(ms, currentDepth, microservices)

                    for(commMS in innerMS){

                        // hope is done when comm ms executes

                        logger.debug{"adding comm ms: $commMS"}

                        val hopDone = currentDepth+1

                        val commExeTime = exePolicy.time(ms, hopDone)

                        val commMeta = mutableMapOf<String, Any>()

                        commMSReqs.add(MSRequest(commMS, commExeTime, commMeta))

                        commSet.add(commMS)

                    }

                }

                msCommMap[MSRequest(ms, exeTime, metaMap)] = commMSReqs

            }

            if(msCommMap.isEmpty()) {

                logger.debug {"empty map breaking"}

                break

            }

            hopsList.add(msCommMap)

            // map after hop zero can have only ms that were previously communicated to

            callingMicroservices = commSet.toList()

        }

        return RouterRequest(0,hopsList.toList())
    }
}
