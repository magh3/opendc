package org.opendc.microservice.simulator.execution

import org.apache.commons.math3.distribution.LogNormalDistribution
import org.opendc.microservice.simulator.microservice.Microservice

public class LogNormalExe(private val m:Double =0.0, private val s:Double =1.0): ExeDelay {

    override fun time(ms: Microservice, hop: Int): Long {

        val logNormal = LogNormalDistribution(m, s)

        var exeTime = logNormal.sample().toLong()

        if(exeTime.toInt() == 0) exeTime = 1

        // exeTime *= 1000

        return exeTime

    }

}
