package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.communication.CommunicationPolicy
import org.opendc.microservice.simulator.execution.ExeDelay
import org.opendc.microservice.simulator.microservice.Microservice
import java.time.Clock
import java.util.*
import kotlin.random.Random

public class RouterRequestGeneratorImpl(private val clock: Clock, private val msListGenerator: CommunicationPolicy,
                                private val exePolicy: ExeDelay,
                                 private val depth: Int = 1): RouterRequestGenerator {

    override fun request(microservices: List<Microservice>): RouterRequest {

        val hopMSMap = mutableListOf<Map<MSRequest, List<MSRequest>>>()

        var outerMS = msListGenerator.communicateMs(Microservice(UUID.randomUUID()), 0, microservices)

        val reqDepth = Random.nextInt(0,depth+1)

        println("making request with depth $reqDepth")

        // runs at least once for 0.

        for(i in 0 until reqDepth){

            // comm for depth i list

            val hopMap = mutableMapOf<MSRequest, List<MSRequest>>()

            val commSet = mutableSetOf<Microservice>()

            var maxExe: Long = 0

            println("outerMS is $outerMS")

            for(ms in outerMS){

                val exeTime = exePolicy.time(ms, i)

                if(exeTime > maxExe) maxExe = exeTime

                val metaMap = mutableMapOf<String, Any>("deadline" to (clock.millis() + ((i+1)*500) ))

                val commMSReqs = mutableListOf<MSRequest>()

                if(i < reqDepth){

                    // communication ms

                    val innerMS = msListGenerator.communicateMs(ms, i, microservices)

                    for(commMS in innerMS){

                        // hope is done when comm ms executes

                        println("adding comm ms: $commMS")

                        val hopDone = i+1

                        val commExeTime = exePolicy.time(ms, hopDone)

                        val commMeta = mutableMapOf<String, Any>("deadline" to (clock.millis() + ((hopDone+1)*500) ))

                        commMSReqs.add(MSRequest(commMS, commExeTime, commMeta))

                        commSet.add(commMS)

                    }

                }

                    //innerMS.map { MSRequest(it, exeTime, metaMap) }

                hopMap[MSRequest(ms, exeTime, metaMap)] = commMSReqs

            }

            if(hopMap.isEmpty()) break

            hopMSMap.add(hopMap)

            // map after hop zero can have only ms that were previously communicated to

            outerMS = commSet.toList()

        }

        return RouterRequest(0,hopMSMap.toList())
    }
}
