package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.coroutines.Continuation

public class RequestV2(private val ms: Microservice,
                private val hops: Int) {

    private lateinit var cont: Continuation<Unit>

    public fun getHops(): Int {

        return hops

    }

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
