package org.opendc.microservice.simulator.stats

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.util.*

public class MSInstanceStats(private val msId: UUID, private val instanceId: UUID){

    private val exeTimeStats: MutableList<Long> = mutableListOf()
    private val waitTimeStats: MutableList<Long> = mutableListOf()
    private val totalTimeStats: MutableList<Long> = mutableListOf()
    private val slowDownStats: MutableList<Long> = mutableListOf()
    private val utilization: MutableList<Double> = mutableListOf()

    public fun saveExeTime(value: Long){

        exeTimeStats.add(value)

    }

    public fun saveWaitTime(value: Long){

        waitTimeStats.add(value)

    }

    public fun saveTotalTime(value: Long){

        totalTimeStats.add(value)

    }

    public fun saveSlowDown(value: Long){

        slowDownStats.add(value)

    }

    public fun saveUtilization(value: Double){

        utilization.add(value)

    }

    override fun toString(): String {

        return "Stats for microservice $msId Instance $instanceId are: \n" +
            "Execution times (${exeTimeStats.size}) : $exeTimeStats\n" +
            "Wait times (${waitTimeStats.size}): $waitTimeStats \n" +
            "Total times (${totalTimeStats.size}): $totalTimeStats. \n" +
            "Slow down (${slowDownStats.size}): $slowDownStats \n" +
            "Utilization at configured intervals: $utilization \n\n"

    }

}
