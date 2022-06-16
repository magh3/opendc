package org.opendc.microservice.simulator.microservice

import mu.KotlinLogging
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.opendc.microservice.simulator.state.RegistryManager
import java.time.Clock
import java.util.*

public class Microservice(private val id: UUID, private val registryManager: RegistryManager,
                          private val clock: Clock, private val simDuration: Long){

    private var exeTimeStat: Long = 0

    private val logger = KotlinLogging.logger {}

    public fun getId(): UUID {

        return id

    }

    public fun saveExeTime(exeTime: Long){

        val nrOfInstances = registryManager.getInstances(this).size

        val utilization = exeTimeStat.toDouble()/(nrOfInstances * clock.millis())

        if(utilization > 1.0) println("for ms ${getId()} utilization  is $utilization")

        exeTimeStat += exeTime

    }

    public fun getUtilization(): Double {

        val nrOfInstances = registryManager.getInstances(this).size

        logger.debug { "MS: ${getId()} has $nrOfInstances instances" }

        // println("${getId()} total exe time is $exeTimeStat")

        return exeTimeStat.toDouble()/ (nrOfInstances * simDuration)

    }

    override fun equals(other: Any?): Boolean = other is Microservice && id == other.id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {

        return id.toString()

    }

}
