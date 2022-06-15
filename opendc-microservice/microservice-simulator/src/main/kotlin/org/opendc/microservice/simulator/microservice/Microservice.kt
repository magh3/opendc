package org.opendc.microservice.simulator.microservice

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.util.*

public class Microservice(private val id: UUID){

    private var exeTimeStat = DescriptiveStatistics()//.apply{ windowSize = 100 }

    public fun getId(): UUID {

        return id

    }

    public fun saveExeTime(exeTime: Long){

        exeTimeStat.addValue(exeTime.toDouble())

    }

    public fun getUtilization(): Double {

        return exeTimeStat.values.sum()/(2*(1000*3600*2))

    }

    override fun equals(other: Any?): Boolean = other is Microservice && id == other.id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {

        return id.toString()

    }

}
