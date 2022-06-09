package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.communication.CommunicationPolicy
import org.opendc.microservice.simulator.execution.ExeDelay
import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*

class RouterRequestGeneratorImpl(private val msListGenerator: CommunicationPolicy,
                                private val exePolicy: ExeDelay,
                                 private val depth: Int = 1): RouterRequestGenerator {

    override fun request(microservices: List<Microservice>): RouterRequest {

        val hopMSMap = mutableListOf<Map<MSRequest, List<MSRequest>>>()

        val hopMap = mutableMapOf<MSRequest, List<MSRequest>>()

        var outerMS = msListGenerator.communicateMs(Microservice(UUID.randomUUID()), 0, microservices)

        for(i in 0..depth){

            // comm for depth i list

            val commSet = mutableSetOf<Microservice>()

            for(ms in outerMS){

                val innerMS = msListGenerator.communicateMs(Microservice(UUID.randomUUID()), 0, microservices)

                val msReqs = innerMS.map { MSRequest(it, exePolicy.time(ms, i), mapOf<String, Any>()) }

                hopMap[MSRequest(ms, exePolicy.time(ms, i), mapOf<String, Any>())] = msReqs

                commSet.add(ms)

            }

            // map after hop zero can have only ms that were previously communicated to

            outerMS = commSet.toList()

        }

        return RouterRequest(0,hopMSMap.toList())
    }
}
