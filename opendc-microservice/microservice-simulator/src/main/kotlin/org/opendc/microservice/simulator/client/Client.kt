package org.opendc.microservice.simulator.client

import org.opendc.microservice.simulator.mapping.MicroserviceMapPolicy
import org.opendc.microservice.simulator.router.ForwardPolicy
import org.opendc.microservice.simulator.router.RequestRouter


public class Client(private val id: Int){

    public fun sendRequest(forwardPolicy: ForwardPolicy,
                           mapPolicy: MicroserviceMapPolicy){

        val requestRouter = RequestRouter()

        requestRouter.processRequest(forwardPolicy, mapPolicy)

    }

}
