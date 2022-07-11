package org.opendc.microservice.simulator.microservice

import mu.KotlinLogging
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.opendc.microservice.simulator.state.RegistryManager
import java.util.*

public class Microservice(
    private val id: UUID, private val registryManager: RegistryManager
){

    private var utilizationExeTime: Long = 0
    private val utilization = DescriptiveStatistics()// .apply{ windowSize = 100 }
    private val logger = KotlinLogging.logger {}

    public fun getId(): UUID {
        return id
    }

    public fun saveExeTime(exeTime: Long){
        utilizationExeTime += exeTime
    }


    public fun setUtilization(){
        val nrOfInstances = registryManager.getInstances(this).size
        utilization.addValue(utilizationExeTime.toDouble()/(nrOfInstances * 1 * 3600 * 1000))
        utilizationExeTime = 0
    }


    public fun getUtilization(): DoubleArray {
        return utilization.values
    }

    override fun equals(other: Any?): Boolean = other is Microservice && id == other.id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return id.toString()
    }

}
