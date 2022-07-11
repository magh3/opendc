package org.opendc.microservice.simulator.microservice

import java.util.*

/**
 * Not using this
 */
public class MSConfiguration(private val id: UUID,
                             private val instanceIds: List<UUID>,
                             ){

    public fun getId(): UUID {
        return id
    }

    public fun getInstanceIds(): List<UUID> {
        return instanceIds
    }

    override fun toString(): String {
        return "MS Config - ms: ${getId()} with instances ${getInstanceIds()} \n"
    }

}
