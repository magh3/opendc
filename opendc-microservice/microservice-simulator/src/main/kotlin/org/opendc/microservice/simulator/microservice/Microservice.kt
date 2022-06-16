package org.opendc.microservice.simulator.microservice

import mu.KotlinLogging
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.opendc.microservice.simulator.state.RegistryManager
import java.time.Clock
import java.util.*

public class Microservice(private val id: UUID, private val registryManager: RegistryManager,
                          private val clock: Clock, private val simDuration: Long){

    private var exeTimeStat: Long = 0

    private val utilization = DescriptiveStatistics()// .apply{ windowSize = 100 }

    private val logger = KotlinLogging.logger {}

    public fun getId(): UUID {

        return id

    }

    public fun saveExeTime(exeTime: Long){

        exeTimeStat += exeTime

    }


    public fun setUtilization(){

        val nrOfInstances = registryManager.getInstances(this).size

        utilization.addValue(exeTimeStat.toDouble()/(nrOfInstances * 1 * 3600 * 1000))

        exeTimeStat = 0

    }


    public fun getUtilization(): DoubleArray {

        // val nrOfInstances = registryManager.getInstances(this).size

        // logger.debug { "MS: ${getId()} has $nrOfInstances instances" }

        // println("${getId()} total exe time is $exeTimeStat")

        // return exeTimeStat.toDouble()/ (nrOfInstances * simDuration)

        return utilization.values

    }

    override fun equals(other: Any?): Boolean = other is Microservice && id == other.id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {

        return id.toString()

    }

}
