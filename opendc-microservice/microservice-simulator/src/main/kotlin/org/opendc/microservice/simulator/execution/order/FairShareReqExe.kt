package org.opendc.microservice.simulator.execution.order

import kotlinx.coroutines.delay
import org.opendc.microservice.simulator.execution.RequestExecution
import org.opendc.microservice.simulator.microservice.MSInstance

public class FairShareReqExe(private val shareDelay:Long = 500): RequestExecution {

    override suspend fun execute(queueEntry: MSInstance.InvocationRequest): Boolean {

        // set meta if first time

        if(queueEntry.msReq.getMeta()["exeLeft"] == null) {

            queueEntry.msReq.setMeta("exeLeft", queueEntry.msReq.getExeTime())

        }

        val exeTime = queueEntry.msReq.getMeta()["exeLeft"] as Long

        if(exeTime > shareDelay) {

            // request does not finish

            delay(shareDelay)

            // update leftover exe time

            val exeLeft = exeTime - shareDelay

            queueEntry.msReq.setMeta("exeLeft", exeLeft)

            // put at end of queue so return false

            return false

        }
        else{

            // finishes

            delay(exeTime)

        }

        return true

    }

}
