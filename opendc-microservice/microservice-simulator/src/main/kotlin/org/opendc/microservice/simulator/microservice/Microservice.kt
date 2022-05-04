package org.opendc.microservice.simulator.microservice

import kotlinx.coroutines.CoroutineScope
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock
import java.util.*

public class Microservice(private val id: UUID,
                          private var nrOfInstances: Int,
                          private val clock: Clock,
                          private val scope: CoroutineScope,
                          private val model: MachineModel,
                          private val getExtRetProb: Double = 100.0,
                          private val getIntReqProb: Double = 100.0,
                          private val sendIntReqProb: Double = 100.0
                          ){

    private val instanceGenerator = MicroserviceInstanceGenerator(clock, scope, model)

    private var instances: Array<MicroserviceInstance> = instanceGenerator.generate(nrOfInstances)

    public fun getId(): UUID {

        return id

    }

    public fun getInstances(): Array<MicroserviceInstance> {

        return instances

    }


    override fun equals(other: Any?): Boolean = other is Microservice && id == other.id

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + nrOfInstances
        result = 31 * result + getExtRetProb.hashCode()
        result = 31 * result + getIntReqProb.hashCode()
        result = 31 * result + sendIntReqProb.hashCode()
        result = 31 * result + instanceGenerator.hashCode()
        result = 31 * result + instances.contentHashCode()
        return result
    }


}
