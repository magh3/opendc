package org.opendc.microservice.simulator.microservice

import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.metrics.Meter
import kotlinx.coroutines.CoroutineScope
import org.opendc.simulator.compute.model.MachineModel
import java.time.Clock
import java.util.*

public class Microservice(private val id: UUID,
                          private var nrOfInstances: Int,
                          private val clock: Clock,
                          private val scope: CoroutineScope,
                          private val model: MachineModel,
                          meter: Meter,
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

    /**
     * The total amount of function invocations received by the function.
     */
    public val invocations: LongCounter = meter.counterBuilder("function.invocations.total")
        .setDescription("Number of function invocations")
        .setUnit("1")
        .build()


    override fun equals(other: Any?): Boolean = other is Microservice && id == other.id

    override fun hashCode(): Int = id.hashCode()


}
