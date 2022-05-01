package org.opendc.microservice.simulator.mapping

import org.opendc.microservice.simulator.microservice.Microservice

public interface MicroserviceMapPolicy {

    /*
    * returns id of microservice to map to
    * */
    public fun mapsTo():Microservice

}
