package org.opendc.microservice.simulator.communication

import org.opendc.microservice.simulator.microservice.Microservice

public interface CommunicationPolicy{

    /**
     * Not using right now
     */

    public fun communicate(ms: Microservice):List<Microservice>

}
