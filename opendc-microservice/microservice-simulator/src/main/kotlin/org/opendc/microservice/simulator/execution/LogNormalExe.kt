package org.opendc.microservice.simulator.execution

import org.apache.commons.math3.distribution.LogNormalDistribution
import org.apache.commons.math3.random.RandomDataGenerator
import org.apache.commons.math3.random.RandomGeneratorFactory
import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*

public class LogNormalExe(private val m:Double =0.0, private val s:Double =1.0): ExeDelay {

    override fun time(ms: Microservice, hop: Int): Long {

        val randGen = RandomDataGenerator(RandomGeneratorFactory.createRandomGenerator(Random(0))).randomGenerator

        val logNormal = LogNormalDistribution(randGen, m, s)

        var exeTime = logNormal.sample().toLong()

        if(exeTime.toInt() == 0) exeTime = 1

        exeTime *= 1000

        return exeTime

    }

}
