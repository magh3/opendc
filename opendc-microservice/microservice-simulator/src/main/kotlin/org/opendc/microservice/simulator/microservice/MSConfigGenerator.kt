package org.opendc.microservice.simulator.microservice

import java.util.*

public class MSConfigGenerator {

    public fun generate(nrOfMS: Int, nrOfInstances: Int): List<MSConfiguration> {

        // plus one for ms id

        val idsCount = nrOfMS * (nrOfInstances + 1)

        // to prevent infinite loop

        val maxIterations = idsCount * 2

        var iterations = 0

        val ids = mutableSetOf<UUID>()

        while( ids.size < idsCount && iterations < maxIterations){

            ids.add(UUID.randomUUID())

            iterations += 1

        }

        require(ids.size == idsCount){"Error generating microservice configs"}

        val msConfigs = mutableListOf<MSConfiguration>()

        for(i in 1..nrOfMS){

            val instanceIds = mutableListOf<UUID>()

            for(j in 1..nrOfInstances){

                instanceIds.add(ids.first().also{ ids.remove(it) })

            }

            val msId = ids.first().also{ ids.remove(it) }

            val config = MSConfiguration(msId, instanceIds)

            msConfigs.add(config)

        }

        return msConfigs.toList()

    }


    public fun generateV2(nrOfMS: Int, nrOfInstances: List<Int>): List<MSConfiguration> {

        require(nrOfMS == nrOfInstances.size){"MS Generation error invalid configuration."}

        // 4 ms with instances 2,3,1,2 so total ids = 2+3+1+2+4

        val idsCount = nrOfMS + nrOfInstances.sum()

        // to prevent infinite loop

        val maxIterations = idsCount * 2

        var iterations = 0

        val ids = mutableSetOf<UUID>()

        while( ids.size < idsCount && iterations < maxIterations){

            ids.add(UUID.randomUUID())

            iterations += 1

        }

        require(ids.size == idsCount){"Error generating microservice configs"}

        val msConfigs = mutableListOf<MSConfiguration>()

        for(i in 0 until nrOfMS){

            val instanceIds = mutableListOf<UUID>()

            for(j in 1..nrOfInstances[i]){

                instanceIds.add(ids.first().also{ ids.remove(it) })

            }

            val msId = ids.first().also{ ids.remove(it) }

            val config = MSConfiguration(msId, instanceIds)

            msConfigs.add(config)

        }

        return msConfigs.toList()

    }

}
