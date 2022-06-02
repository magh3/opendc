package org.opendc.microservice.simulator.communication

import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*

public interface CommunicationPolicy{

    /**
     * Not using right now
     */

    public fun communicateMs(msID: UUID, hopsDone: Int, microservices: MutableList<Microservice>):List<Microservice>

}
