package org.opendc.microservice.simulator.trace

import java.io.File
import java.util.stream.Collectors


public class MSTraceReader {

    public fun parse(path: File): MutableList<MSTraceRow> {

        val trace = mutableListOf<MSTraceRow>()

        var idx = 0

        path.forEachLine { line ->

            val values = line.split(",")

            /* No Header parsing */
            if (idx != 0) {

                val instanceId = values[1].trim().toString()
                val endTime = values[2].trim().toFloat()
                val exeTime = values[3].trim().toFloat()
                val startTime = endTime - exeTime

                trace.add(MSTraceRow(instanceId, endTime, exeTime, startTime))

            }


            idx += 1

        }

        // val counts: Map<Int, Long> = listOf(1,2).stream().collect(Collectors.groupingBy({ e -> e }, Collectors.counting()))

        return trace

    }

}
