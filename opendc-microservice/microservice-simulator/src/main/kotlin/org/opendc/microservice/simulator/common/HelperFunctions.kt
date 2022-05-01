package org.opendc.microservice.simulator.common

public class HelperFunctions {

    public fun <T> hasDuplicates(arr: Array<T>): Boolean {
        return arr.size != arr.distinct().count();
    }

}
