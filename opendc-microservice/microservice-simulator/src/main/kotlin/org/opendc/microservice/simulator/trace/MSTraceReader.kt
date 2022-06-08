package org.opendc.microservice.simulator.trace

import java.io.File

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

        return trace

    }

}
