package org.opendc.microservice.simulator.microservice

class Microservice(private val id: String, private var nrOfInstances: Int){

    private val microserviceGenerator = MicroserviceInstanceGenerator()

    private var instances: Array<MicroserviceInstance> = microserviceGenerator.generateInstances(nrOfInstances)

    public fun getId(): String{

        return id

    }

    public fun getInstances(): Array<MicroserviceInstance> {

        return instances

    }


}
