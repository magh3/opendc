package org.opendc.microservice.simulator.workload

import org.opendc.simulator.compute.workload.SimWorkload

/**
 * A model for a microservice workload, which may be invoked multiple times.
 */
public interface MSWorkload: SimWorkload {

    /**
     * This method is invoked when an active microservice instance is invoked.
     */
    public suspend fun invoke(exeTime: Long)

}
