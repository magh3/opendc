package org.opendc.microservice.simulator.router

import java.time.Clock

public class Request(clock: Clock){

    private val arrivalTime = clock.instant()

    private val forwardNr: Int = 0

}
