package org.opendc.microservice.simulator.microservice

import java.util.*

class MSConfigGenerator {

    public fun generate(nrOfMS: Int, nrOfInstances: Int): List<MSConfiguration> {

        val idsCount = nrOfMS * nrOfInstances

        val maxIterations = nrOfMS * 2

        var iterations = 0

        val ids = mutableSetOf<UUID>()

        while( ids.size < idsCount && iterations < maxIterations){

            ids.add(UUID.randomUUID())

            iterations += 1

        }

        require(ids.size != idsCount){"Error generating microservice configs"}

        val msConfigs = mutableListOf<MSConfiguration>()

        var config: MSConfiguration

        for(i in 1..nrOfMS){

            val instanceIds = mutableListOf<UUID>()

            for(j in 1..nrOfInstances){

                instanceIds.add(ids.first().also{ ids.remove(it) })

            }

            config = MSConfiguration(ids.first().also{ ids.remove(it) }, instanceIds)

            msConfigs.add(config)

        }

        return msConfigs.toList()

    }

}
