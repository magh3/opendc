package org.opendc.microservice.simulator.stats

import java.util.*

public class RouterStats(private val exeTimeStats: MutableList<Long>,
                         private val waitTimeStats: MutableList<Long>,
                         private val totalTimeStats: MutableList<Long>,
                         private val slowDownStats: MutableList<Long>
) {

    override fun toString(): String {

        return "Stats for Router per full request are: \n" +
            "Execution times (${exeTimeStats.size}) : $exeTimeStats\n" +
            "Wait times (${waitTimeStats.size}): $waitTimeStats \n" +
            "Total times (${totalTimeStats.size}): $totalTimeStats. \n" +
            "Slow down (${slowDownStats.size}): $slowDownStats \n\n"

    }

}
