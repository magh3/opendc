package org.opendc.microservice.simulator.common

import org.opendc.simulator.compute.workload.SimWorkload


public interface SimMSWorkload: SimWorkload {

    /**
     * This method is invoked when an active microservice instance is invoked.
     */
    public suspend fun invoke()

}
