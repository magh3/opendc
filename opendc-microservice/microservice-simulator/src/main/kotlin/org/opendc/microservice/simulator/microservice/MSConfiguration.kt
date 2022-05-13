package org.opendc.microservice.simulator.microservice

import java.util.*

/**
 * Not using this
 */
public class MSConfiguration(private val id: UUID,
                             private var instanceIds: List<UUID>,

                             ){

    public fun getId(): UUID {

        return id

    }


    public fun getInstanceIds(): List<UUID> {

        return instanceIds

    }

}
