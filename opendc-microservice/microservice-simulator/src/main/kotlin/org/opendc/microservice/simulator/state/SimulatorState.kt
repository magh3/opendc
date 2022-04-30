package org.opendc.microservice.simulator.state

import org.opendc.microservice.simulator.router.ForwardPolicy

public class SimulatorState
    (private val nrOfMicroservices: Int,
    private val forwardPolicy: ForwardPolicy
     ) {
}
