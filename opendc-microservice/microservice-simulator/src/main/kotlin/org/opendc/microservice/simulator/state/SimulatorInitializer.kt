package org.opendc.microservice.simulator.state

import org.opendc.microservice.simulator.mapping.MicroserviceMapPolicy
import org.opendc.microservice.simulator.microservice.Microservice
import org.opendc.microservice.simulator.microservice.MicroserviceGenerator
import org.opendc.microservice.simulator.router.ForwardPolicy


public class SimulatorInitializer
    (private val microserviceIds: Array<String>,
     private val microserviceInstances: Array<Int>,
     private val requestRate: Double,
    private val forwardPolicy: ForwardPolicy,
     private val microserviceMapper: MicroserviceMapPolicy,
     ) {

    private val microservices = initializeMicroservices()


    private fun initializeMicroservices(): Array<Microservice> {

        return MicroserviceGenerator().generateMicroservices(microserviceIds, microserviceInstances)

    }

}
