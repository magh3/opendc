package org.opendc.microservice.simulator.execution

import org.opendc.microservice.simulator.microservice.Microservice

public interface ExeDelay {

    /**
     * returns time of execution
     */
    public fun time(ms: Microservice, hop: Int): Long

}
