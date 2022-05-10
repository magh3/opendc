package org.opendc.microservice.simulator.microservice

import java.util.*

public class MicroserviceConfiguration(private val id: UUID,
                                       private var nrOfInstances: Int,
                                       private val getExtRetProb: Double,
                                       private val getIntReqProb: Double,
                                       private val sendIntReqProb: Double
){

    public fun getId(): UUID = id

    public fun getInstances(): Int = nrOfInstances

}