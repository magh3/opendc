package org.opendc.microservice.simulator.router

import org.apache.commons.math3.distribution.PoissonDistribution
import org.apache.commons.math3.random.RandomDataGenerator
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.RandomGeneratorFactory
import java.util.*

public class PoissonDelay(rate: Double): InterArrivalDelay {

    private val randgen = RandomDataGenerator(RandomGeneratorFactory.createRandomGenerator(Random(0))).randomGenerator

    private val dist: PoissonDistribution = PoissonDistribution(randgen,rate, PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS)


    override fun time(): Long {

        return this.dist.sample().toLong()

    }


}
