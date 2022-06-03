package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.coroutines.Continuation

public class Request(private val ms: Microservice,
                     private val hops: Int) {

    private lateinit var cont: Continuation<Unit>

    /**
     * returns hopes done
     */
    public fun getHops(): Int {

        return hops

    }

    /**
     * returns microservice requested
     */
    public fun ms(): Microservice {

        return ms

    }

    public fun setCont(firstCont: Continuation<Unit>){

        cont = firstCont

    }


    public fun getCont(): Continuation<Unit> {

        return cont

    }

}
