package org.opendc.microservice.simulator.router

import org.apache.commons.math3.distribution.PoissonDistribution


public class PoissonArrival(rate: Double) {

    private val dist: PoissonDistribution = PoissonDistribution(rate)

    public fun getSample(): Int {

        return this.dist.sample()

    }

}
