package org.opendc.microservice.simulator.microservice

class Microservice(private val id: String, private var nrOfInstances: Int){

    private var instances: MicroserviceInstance = MicroserviceInstance("1.2.2.3")

    public fun getId(): String{

        return id

    }

    public fun getInstances():MicroserviceInstance{


    }



}
