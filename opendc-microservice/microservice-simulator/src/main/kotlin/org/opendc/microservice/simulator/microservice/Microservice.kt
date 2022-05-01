package org.opendc.microservice.simulator.microservice

public class Microservice(private val id: String, private var nrOfInstances: Int){

    private val instanceGenerator = MicroserviceInstanceGenerator()

    private var instances: Array<MicroserviceInstance> = instanceGenerator.generate(nrOfInstances)

    public fun getId(): String{

        return id

    }

    public fun getInstances(): Array<MicroserviceInstance> {

        return instances

    }


}
