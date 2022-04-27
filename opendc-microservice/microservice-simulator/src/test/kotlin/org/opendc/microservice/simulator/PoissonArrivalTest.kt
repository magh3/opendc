package org.opendc.microservice.simulator

import org.junit.jupiter.api.Test


internal class PoissonArrivalTest {

    @Test
    private fun checkPoisson(){

        val poissonDist = PoissonArrival(5.0)

        print(poissonDist.getSample())

    }

}
