package org.opendc.microservice.simulator.execution

import org.opendc.microservice.simulator.microservice.MSInstance
import org.opendc.microservice.simulator.router.RouterRequest
import java.time.Clock
import java.util.*

public class FirstComeFirstServe(): QueuePolicy {

    override fun getEntry(queue: Queue<MSInstance.InvocationRequest>): Queue<MSInstance.InvocationRequest> {

        return queue

    }

    override fun setMeta(request: RouterRequest, sla: Int, clock: Clock) {

    }


}
