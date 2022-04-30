package org.opendc.microservice.simulator.microservice

import kotlin.random.Random

class MicroserviceInstanceGenerator {

    public fun generateInstances(nrOfInstances: Int): Array<MicroserviceInstance>{

        var instances: Array<MicroserviceInstance> = arrayOf()

        for(i in 1..nrOfInstances){

            instances += MicroserviceInstance(ipGenerator())

        }

        return instances

    }


    /**
     * Generates random ip
     */
    private fun ipGenerator(): String{

        val rand = Random

        var ip: String = ""

        for(i in 1..4){

            if(i > 1) ip = ip+'.'+rand.nextInt(10, 100)

        }

        return ip

    }

}
