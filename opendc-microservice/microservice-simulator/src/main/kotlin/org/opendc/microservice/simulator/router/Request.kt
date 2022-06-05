package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.coroutines.Continuation

public class Request(private val ms: Microservice,
                     private val hops: Int) {

    private lateinit var cont: Continuation<Int>

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

    public fun setCont(firstCont: Continuation<Int>){

        cont = firstCont

    }


    public fun getCont(): Continuation<Int> {

        return cont

    }

}
