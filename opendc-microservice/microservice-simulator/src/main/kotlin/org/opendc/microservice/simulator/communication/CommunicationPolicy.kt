package org.opendc.microservice.simulator.communication

import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*

public interface CommunicationPolicy{

    /**
     * Not using right now
     */

    public fun communicateMs(ms: Microservice, hopsDone: Int, microservices: List<Microservice>):List<Microservice>

}
