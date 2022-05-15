package org.opendc.microservice.simulator.router

import org.apache.commons.math3.distribution.PoissonDistribution


public class PoissonArrival(rate: Double): RequestPolicy {

    private val dist: PoissonDistribution = PoissonDistribution(rate)

    override fun nrOfRequests(): Int {

        return this.dist.sample()

    }

}
