package org.opendc.microservice.simulator.execution

import org.apache.commons.math3.distribution.LogNormalDistribution
import org.apache.commons.math3.random.RandomDataGenerator
import org.apache.commons.math3.random.RandomGeneratorFactory
import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*

public class LogNormalExe(private val m:Double =0.0, private val s:Double =1.0): ExeDelay {

    private val randGen = RandomDataGenerator(RandomGeneratorFactory.createRandomGenerator(Random(0))).randomGenerator

    private val logNormal = LogNormalDistribution(randGen, m, s)


    override fun time(ms: Microservice, hop: Int): Long {

        var exeTime = logNormal.sample().toLong()

        if(exeTime.toInt() == 0) exeTime = 1

        exeTime *= 1000

        return exeTime

    }

}
