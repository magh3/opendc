package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.coroutines.Continuation

class MSRequest(private val ms: Microservice,
                private val exeTime: Long,
                private val meta: Map<String, Any>){

    private lateinit var cont: Continuation<Unit>


    public fun getMS(): Microservice {
        return ms
    }


    public fun getCont(): Continuation<Unit> {

        return cont

    }


    public fun getExeTime(): Long {

        return exeTime

    }


    public fun setCont(continuation: Continuation<Unit>){

        cont = continuation

    }


    override fun equals(other: Any?): Boolean {
        return other is MSRequest && ms == other.getMS()
    }

    override fun hashCode(): Int = ms.getId().hashCode()

}
