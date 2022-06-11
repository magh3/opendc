package org.opendc.microservice.simulator.stats

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.util.*

public class RouterStats(private val exeTimeStats: DescriptiveStatistics,
                  private val waitTimeStats: DescriptiveStatistics,
                 private val totalTimeStats: DescriptiveStatistics,
                  private val slowDownStats: DescriptiveStatistics
) {

    override fun toString(): String {

        return "Stats for Router per full request are: \n" +
            "Execution times (${exeTimeStats.values.size}) : ${Arrays.toString(exeTimeStats.values)} \n" +
            "Wait times (${waitTimeStats.values.size}): ${Arrays.toString(waitTimeStats.values)} \n" +
            "Total times (${totalTimeStats.values.size}): ${Arrays.toString(totalTimeStats.values)} \n" +
            "Slow down (${slowDownStats.values.size}): ${Arrays.toString(slowDownStats.values)} \n\n"

    }

}
