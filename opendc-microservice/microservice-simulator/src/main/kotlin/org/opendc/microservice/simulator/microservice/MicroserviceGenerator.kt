package org.opendc.microservice.simulator.microservice

import org.opendc.microservice.simulator.common.HelperFunctions

public class MicroserviceGenerator {

    public fun generateMicroservices(ids: Array<String>, nrOfInstances: Array<Int>): Array<Microservice>{

        // check if empty

        if(ids.isEmpty()){

            println("No Microservice Ids provided")

            return arrayOf()

        }

        // check duplicates

        val helperFunctions = HelperFunctions()

        if(helperFunctions.hasDuplicates(ids)){

            println("Microservice Ids must be unique")

            return arrayOf()

        }

        // no duplicates and non empty

        var microservices: Array<Microservice> = arrayOf()

        for(i in 1..ids.size){

            microservices += Microservice(ids[i], nrOfInstances[i])

        }

        return microservices

    }

}
