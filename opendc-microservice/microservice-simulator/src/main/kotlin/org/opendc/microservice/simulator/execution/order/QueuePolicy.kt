package org.opendc.microservice.simulator.execution.order

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.router.RouterRequest
import java.time.Clock
import java.util.*

public interface QueuePolicy {

    public fun getEntry(queue: Queue<MSInstance.InvocationRequest>): Queue<MSInstance.InvocationRequest>

    public fun setMeta(request: RouterRequest, sla: Int, clock: Clock)

}
