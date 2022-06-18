package org.opendc.microservice.simulator.router

import org.opendc.microservice.simulator.microservice.Microservice
import kotlin.coroutines.Continuation

public class MSRequest(private val ms: Microservice,
                       private var exeTime: Long,
                       private val meta: MutableMap<String, Any>){

    private lateinit var cont: Continuation<Int>


    public fun getMS(): Microservice {
        return ms
    }


    public fun getCont(): Continuation<Int> {

        return cont

    }


    public fun getExeTime(): Long {

        return exeTime

    }


    public fun updateExeTime(newExeTime: Long){

        exeTime = newExeTime

    }


    public fun getMeta(): MutableMap<String, Any> {

        return meta

    }


    public fun setCont(continuation: Continuation<Int>){

        cont = continuation

    }


    public fun setMeta(key: String, value: Any){

        meta[key] = value

    }

    override fun toString(): String {
        return ms.toString()
    }

}
