package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.mapping.MicroserviceMapPolicy


class RequestRouter() {

    public fun processRequest(forwardPolicy: ForwardPolicy,
                              mapPolicy: MicroserviceMapPolicy){

        val forwardNr = forwardPolicy.getForwardNr()

        println("Request will be forwarded to $forwardNr microservices")

    }

}
