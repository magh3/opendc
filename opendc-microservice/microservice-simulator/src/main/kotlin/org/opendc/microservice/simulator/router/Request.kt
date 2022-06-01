package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice

public class Request(private val invocations: List<Microservice>) {

    private var currentInvokeIndex = 0


    init{

        // check list should have distinct microservices

        require(invocations.map { it.getId() }.toSet().size == invocations.size){"ERROR. Invocations should be unique"}

    }


    public fun getCurrentMS(): Int {

        return currentInvokeIndex + 1
    }


    public fun getReqInvocations(): List<Microservice> {

        return invocations

    }


    public fun isNext(): Boolean {

        return currentInvokeIndex < invocations.size

    }


    public fun next(): Microservice? {

        // check if next exist

        if(!isNext()){

            println("no next returning null")

            return null

        }

        val nextMS = invocations[currentInvokeIndex]

        currentInvokeIndex += 1

        return nextMS

    }

}
