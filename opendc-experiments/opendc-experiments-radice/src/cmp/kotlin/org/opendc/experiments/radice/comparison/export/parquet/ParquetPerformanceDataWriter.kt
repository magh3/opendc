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

package org.opendc.experiments.radice.comparison.export.parquet

import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.GenericRecordBuilder
import org.opendc.compute.workload.export.parquet.ParquetDataWriter
import org.opendc.experiments.radice.comparison.export.PerformanceData
import java.io.File

/**
 * A writer for experiment reports in a Parquet format.
 *
 * @param path The path to write the data to.
 */
class ParquetPerformanceDataWriter(path: File) : ParquetDataWriter<PerformanceData>(path, SCHEMA) {
    override fun convert(builder: GenericRecordBuilder, data: PerformanceData) {
        builder["repeat"] = data.repeat
        builder["duration"] = data.duration
        builder["mem_usage"] = data.memoryUsage.used
        builder["mem_usage_peak"] = data.memoryPeakUsage.used
        builder["mem_committed"] = data.memoryUsage.committed
        builder["mem_committed_peak"] = data.memoryPeakUsage.committed
    }

    override fun toString(): String = "experiment-writer"

    private companion object {
        private val SCHEMA: Schema = SchemaBuilder
            .record("risk")
            .namespace("org.opendc.experiments.cloudsim")
            .fields()
            .requiredInt("repeat")
            .requiredLong("duration")
            .requiredLong("mem_usage")
            .requiredLong("mem_usage_peak")
            .requiredLong("mem_committed")
            .requiredLong("mem_committed_peak")
            .endRecord()
    }
}