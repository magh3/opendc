package org.opendc.microservice.simulator.execution

import org.apache.commons.math3.distribution.PoissonDistribution

public class PoissonDelay(rate: Double): InterArrivalDelay{

    private val dist: PoissonDistribution = PoissonDistribution(rate)

    override fun time(): Long {

        return this.dist.sample().toLong()

    }


}
