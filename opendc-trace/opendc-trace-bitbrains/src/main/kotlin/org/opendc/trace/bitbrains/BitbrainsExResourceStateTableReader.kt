/*
 * Copyright (c) 2021 AtLarge Research
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.opendc.trace.bitbrains

import org.opendc.trace.*
import org.opendc.trace.conv.*
import java.io.BufferedReader
import java.time.Instant

/**
 * A [TableReader] for the Bitbrains resource state table.
 */
internal class BitbrainsExResourceStateTableReader(private val reader: BufferedReader) : TableReader {
    override fun nextRow(): Boolean {
        reset()

        var line: String
        var num = 0

        while (true) {
            line = reader.readLine() ?: return false
            num++

            if (line[0] == '#' || line.isBlank()) {
                // Ignore empty lines or comments
                continue
            }

            break
        }

        line = line.trim()

        val length = line.length
        var col = 0
        var start: Int
        var end = 0

        while (end < length) {
            // Trim all whitespace before the field
            start = end
            while (start < length && line[start].isWhitespace()) {
                start++
            }

            end = line.indexOf(' ', start)

            if (end < 0) {
                end = length
            }

            val field = line.subSequence(start, end) as String
            when (col++) {
                COL_TIMESTAMP -> timestamp = Instant.ofEpochSecond(field.toLong(10))
                COL_CPU_USAGE -> cpuUsage = field.toDouble()
                COL_CPU_DEMAND -> cpuDemand = field.toDouble()
                COL_DISK_READ -> diskRead = field.toDouble()
                COL_DISK_WRITE -> diskWrite = field.toDouble()
                COL_CLUSTER_ID -> cluster = field.trim()
                COL_NCPUS -> cpuCores = field.toInt(10)
                COL_CPU_READY_PCT -> cpuReadyPct = field.toDouble()
                COL_POWERED_ON -> poweredOn = field.toInt(10) == 1
                COL_CPU_CAPACITY -> cpuCapacity = field.toDouble()
                COL_ID -> id = field.trim()
                COL_MEM_CAPACITY -> memCapacity = field.toDouble() * 1000 // Convert from MB to KB
            }
        }

        return true
    }

    override fun resolve(column: TableColumn<*>): Int = columns[column] ?: -1

    override fun isNull(index: Int): Boolean {
        require(index in 0..COL_MAX) { "Invalid column index" }
        return false
    }

    override fun get(index: Int): Any? {
        return when (index) {
            COL_ID -> id
            COL_CLUSTER_ID -> cluster
            COL_TIMESTAMP -> timestamp
            COL_NCPUS -> getInt(index)
            COL_POWERED_ON -> getInt(index)
            COL_CPU_CAPACITY, COL_CPU_USAGE, COL_CPU_USAGE_PCT, COL_CPU_READY_PCT, COL_CPU_DEMAND, COL_MEM_CAPACITY, COL_DISK_READ, COL_DISK_WRITE -> getDouble(index)
            else -> throw IllegalArgumentException("Invalid column")
        }
    }

    override fun getBoolean(index: Int): Boolean {
        return when (index) {
            COL_POWERED_ON -> poweredOn
            else -> throw IllegalArgumentException("Invalid column")
        }
    }

    override fun getInt(index: Int): Int {
        return when (index) {
            COL_NCPUS -> cpuCores
            else -> throw IllegalArgumentException("Invalid column")
        }
    }

    override fun getLong(index: Int): Long {
        throw IllegalArgumentException("Invalid column")
    }

    override fun getDouble(index: Int): Double {
        return when (index) {
            COL_CPU_CAPACITY -> cpuCapacity
            COL_CPU_USAGE -> cpuUsage
            COL_CPU_USAGE_PCT -> cpuUsage / cpuCapacity
            COL_CPU_READY_PCT -> cpuReadyPct
            COL_CPU_DEMAND -> cpuDemand
            COL_MEM_CAPACITY -> memCapacity
            COL_DISK_READ -> diskRead
            COL_DISK_WRITE -> diskWrite
            else -> throw IllegalArgumentException("Invalid column")
        }
    }

    override fun close() {
        reader.close()
    }

    /**
     * State fields of the reader.
     */
    private var id: String? = null
    private var cluster: String? = null
    private var timestamp: Instant? = null
    private var cpuCores = -1
    private var cpuCapacity = Double.NaN
    private var cpuUsage = Double.NaN
    private var cpuDemand = Double.NaN
    private var cpuReadyPct = Double.NaN
    private var memCapacity = Double.NaN
    private var diskRead = Double.NaN
    private var diskWrite = Double.NaN
    private var poweredOn: Boolean = false

    /**
     * Reset the state of the reader.
     */
    private fun reset() {
        id = null
        timestamp = null
        cluster = null
        cpuCores = -1
        cpuCapacity = Double.NaN
        cpuUsage = Double.NaN
        cpuDemand = Double.NaN
        cpuReadyPct = Double.NaN
        memCapacity = Double.NaN
        diskRead = Double.NaN
        diskWrite = Double.NaN
        poweredOn = false
    }

    /**
     * Default column indices for the extended Bitbrains format.
     */
    private val COL_TIMESTAMP = 0
    private val COL_CPU_USAGE = 1
    private val COL_CPU_DEMAND = 2
    private val COL_DISK_READ = 4
    private val COL_DISK_WRITE = 6
    private val COL_CLUSTER_ID = 10
    private val COL_NCPUS = 12
    private val COL_CPU_READY_PCT = 13
    private val COL_POWERED_ON = 14
    private val COL_CPU_CAPACITY = 18
    private val COL_ID = 19
    private val COL_MEM_CAPACITY = 20
    private val COL_CPU_USAGE_PCT = 21
    private val COL_MAX = COL_CPU_USAGE_PCT + 1

    private val columns = mapOf(
        RESOURCE_ID to COL_ID,
        RESOURCE_CLUSTER_ID to COL_CLUSTER_ID,
        RESOURCE_STATE_TIMESTAMP to COL_TIMESTAMP,
        RESOURCE_CPU_COUNT to COL_NCPUS,
        RESOURCE_CPU_CAPACITY to COL_CPU_CAPACITY,
        RESOURCE_STATE_CPU_USAGE to COL_CPU_USAGE,
        RESOURCE_STATE_CPU_USAGE_PCT to COL_CPU_USAGE_PCT,
        RESOURCE_STATE_CPU_DEMAND to COL_CPU_DEMAND,
        RESOURCE_STATE_CPU_READY_PCT to COL_CPU_READY_PCT,
        RESOURCE_MEM_CAPACITY to COL_MEM_CAPACITY,
        RESOURCE_STATE_DISK_READ to COL_DISK_READ,
        RESOURCE_STATE_DISK_WRITE to COL_DISK_WRITE
    )
}
