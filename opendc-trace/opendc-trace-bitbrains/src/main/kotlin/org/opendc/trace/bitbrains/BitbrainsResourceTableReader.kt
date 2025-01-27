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

import com.fasterxml.jackson.dataformat.csv.CsvFactory
import org.opendc.trace.*
import org.opendc.trace.conv.RESOURCE_ID
import java.nio.file.Path

/**
 * A [TableReader] for the Bitbrains resource table.
 */
internal class BitbrainsResourceTableReader(private val factory: CsvFactory, vms: Map<String, Path>) : TableReader {
    /**
     * An iterator to iterate over the resource entries.
     */
    private val it = vms.iterator()

    override fun nextRow(): Boolean {
        reset()

        while (it.hasNext()) {
            val (name, path) = it.next()

            val parser = factory.createParser(path.toFile())
            val reader = BitbrainsResourceStateTableReader(name, parser)
            val idCol = reader.resolve(RESOURCE_ID)

            try {
                if (!reader.nextRow()) {
                    continue
                }

                id = reader.get(idCol) as String
                return true
            } finally {
                reader.close()
            }
        }

        return false
    }

    override fun resolve(column: TableColumn<*>): Int = columns[column] ?: -1

    override fun isNull(index: Int): Boolean {
        check(index in 0..columns.size) { "Invalid column index" }
        return false
    }

    override fun get(index: Int): Any? {
        return when (index) {
            COL_ID -> id
            else -> throw IllegalArgumentException("Invalid column")
        }
    }

    override fun getBoolean(index: Int): Boolean {
        throw IllegalArgumentException("Invalid column")
    }

    override fun getInt(index: Int): Int {
        throw IllegalArgumentException("Invalid column")
    }

    override fun getLong(index: Int): Long {
        throw IllegalArgumentException("Invalid column")
    }

    override fun getDouble(index: Int): Double {
        throw IllegalArgumentException("Invalid column")
    }

    override fun close() {}

    /**
     * State fields of the reader.
     */
    private var id: String? = null

    /**
     * Reset the state of the reader.
     */
    private fun reset() {
        id = null
    }

    private val COL_ID = 0
    private val columns = mapOf(RESOURCE_ID to COL_ID)
}
