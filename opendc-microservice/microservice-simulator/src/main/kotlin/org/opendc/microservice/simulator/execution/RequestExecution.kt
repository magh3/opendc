package org.opendc.microservice.simulator.execution

import org.opendc.microservice.simulator.microservice.MSInstance

public interface RequestExecution {

    suspend public fun execute(queueEntry: MSInstance.InvocationRequest): Boolean

}
