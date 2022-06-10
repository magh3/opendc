package org.opendc.microservice.simulator.execution

import org.opendc.microservice.simulator.microservice.MSInstance
import java.util.*

public class FirstComeFirstServe(): QueuePolicy {

    override fun getEntry(queue: Queue<MSInstance.InvocationRequest>): MSInstance.InvocationRequest {

        return queue.poll()

    }


}
