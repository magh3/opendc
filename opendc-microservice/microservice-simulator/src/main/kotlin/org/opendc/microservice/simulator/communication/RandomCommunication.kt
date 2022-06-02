package org.opendc.microservice.simulator.communication

import org.opendc.microservice.simulator.microservice.Microservice
import java.util.*
import kotlin.random.Random

public class RandomCommunication(private val nrOfMS: Int): CommunicationPolicy {

    override fun communicateMs(msID: UUID, hopsDone: Int, microservices: MutableList<Microservice>): List<Microservice> {

        val callMS = mutableListOf<Microservice>()

        for(i in 1..nrOfMS){

            callMS.add(getRandomMS(microservices))

        }

        return callMS
    }


    private fun getRandomMS(microservices: MutableList<Microservice>): Microservice{

        val nrOfMicroservices: Int = microservices.size

        val randService = Random.nextInt(0, nrOfMicroservices)

        return microservices[randService]

    }

}
