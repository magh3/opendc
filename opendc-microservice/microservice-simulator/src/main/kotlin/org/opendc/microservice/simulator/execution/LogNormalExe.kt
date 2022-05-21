package org.opendc.microservice.simulator.execution

import org.apache.commons.math3.distribution.LogNormalDistribution

public class LogNormalExe(private val m:Double =0.0, private val s:Double =1.0): ExePolicy {

    override fun time(): Long {

        val logNormal = LogNormalDistribution(m, s)

        return logNormal.sample().toLong()

    }

}
