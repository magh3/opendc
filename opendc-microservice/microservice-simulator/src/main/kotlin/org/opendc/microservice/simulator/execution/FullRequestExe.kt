package org.opendc.microservice.simulator.execution

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.opendc.microservice.simulator.microservice.MSInstance

public class FullRequestExe: RequestExecution {

    override suspend fun execute(queueEntry: MSInstance.InvocationRequest): Boolean {
        delay(queueEntry.msReq.getExeTime())
        return true
    }

}
