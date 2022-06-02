package org.opendc.microservice.simulator.microservice

import java.util.*

public class Microservice(private val id: UUID){

    public fun getId(): UUID {

        return id

    }

    override fun equals(other: Any?): Boolean = other is Microservice && id == other.id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {

        return id.toString()

    }

}
