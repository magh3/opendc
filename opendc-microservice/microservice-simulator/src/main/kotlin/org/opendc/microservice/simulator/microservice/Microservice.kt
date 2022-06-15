package org.opendc.microservice.simulator.microservice

import mu.KotlinLogging
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.opendc.microservice.simulator.state.RegistryManager
import java.util.*

public class Microservice(private val id: UUID, private val registryManager: RegistryManager,
                          private val simDuration: Int){

    private var exeTimeStat = DescriptiveStatistics()// .apply{ windowSize = 100 }

    private val logger = KotlinLogging.logger {}

    public fun getId(): UUID {

        return id

    }

    public fun saveExeTime(exeTime: Long){

        exeTimeStat.addValue(exeTime.toDouble())

    }

    public fun getUtilization(): Double {

        val nrOfInstances = registryManager.getInstances(this).size

        logger.debug { "MS: ${getId()} has $nrOfInstances instances" }

        return exeTimeStat.values.sum()/(nrOfInstances * simDuration)

    }

    override fun equals(other: Any?): Boolean = other is Microservice && id == other.id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {

        return id.toString()

    }

}
