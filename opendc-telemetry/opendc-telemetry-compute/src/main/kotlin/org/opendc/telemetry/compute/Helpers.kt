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

package org.opendc.telemetry.compute

import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricProducer
import org.opendc.telemetry.compute.table.ServiceData
import java.time.Instant

/**
 * Collect the metrics of the compute service.
 */
public fun collectServiceMetrics(timestamp: Instant, metricProducer: MetricProducer): ServiceData {
    return extractServiceMetrics(timestamp, metricProducer.collectAllMetrics())
}

/**
 * Extract a [ServiceData] object from the specified list of metric data.
 */
public fun extractServiceMetrics(timestamp: Instant, metrics: Collection<MetricData>): ServiceData {
    lateinit var serviceData: ServiceData
    val agg = ComputeMetricAggregator()
    val monitor = object : ComputeMonitor {
        override fun record(data: ServiceData) {
            serviceData = data
        }
    }

    agg.process(metrics)
    agg.collect(timestamp, monitor)
    return serviceData
}
