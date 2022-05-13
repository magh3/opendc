package org.opendc.microservice.simulator.mapping

import org.opendc.microservice.simulator.microservice.Microservice

public interface MicroserviceMapPolicy {

    /*
    * returns microservice to map to
    * */
    public fun mapsTo(microservices: Array<Microservice>):Microservice

}
