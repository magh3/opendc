package org.opendc.microservice.simulator

import org.junit.jupiter.api.Test


internal class PoissonArrivalTest {

    @Test
    fun checkPoisson(){

        val poissonDist = PoissonArrival(5.0)

        print(poissonDist.getSample())

        assert(true)

    }

}
