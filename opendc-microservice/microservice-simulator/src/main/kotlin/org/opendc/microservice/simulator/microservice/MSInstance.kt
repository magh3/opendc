package org.opendc.microservice.simulator.microservice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.opendc.microservice.simulator.state.RegistryManager
import org.opendc.simulator.compute.SimBareMetalMachine
import org.opendc.simulator.compute.SimMachine
import org.opendc.simulator.compute.SimMachineContext
import org.opendc.simulator.compute.model.MachineModel
import org.opendc.simulator.compute.power.ConstantPowerModel
import org.opendc.simulator.compute.power.SimplePowerDriver
import org.opendc.simulator.compute.workload.SimWorkload
import org.opendc.simulator.flow.FlowEngine
import java.time.Clock
import java.util.*

/**
* Microservice instance has an id.
* The clock, scope, model are used to config the machine to run the instance on.
 */
public class MSInstance(private val msId: UUID,
                        private val id: UUID,
                        private val clock: Clock,
                        private val scope: CoroutineScope,
                        private val model: MachineModel,
                        private val registryManager: RegistryManager
                                  ){

    init{

        registryManager.registerInstance(this)

    }

    public fun getId(): UUID {

        return id

    }


    public fun getMSId(): UUID {

        return msId

    }


    /**
     * The machine that will execute the workloads.
     */
    private val machine: SimMachine = SimBareMetalMachine(
        FlowEngine(scope.coroutineContext, clock),
        model,
        SimplePowerDriver(ConstantPowerModel(0.0))
    )


    /**
     * The job associated with the lifecycle of the instance.
     */
    private var job: Job? = null


    public fun run(){

        job = scope.launch {

            println("launching instance with UUID "+getId())

            machine.startWorkload(object : SimWorkload {

                override fun onStart(ctx: SimMachineContext) {
                }

                override fun onStop(ctx: SimMachineContext) {
                }
            })

        }

    }


    /**
     * run request on instance.
     * if not active, make it active.
     */
    public fun invoke(){

        println("MSInstance invoked with id "+ getId())

    }


    /**
     * stop instance, close / remove.
     */
    public fun close(){

        registryManager.deregisterInstance(this)

    }


    override fun equals(other: Any?): Boolean = other is MSInstance && id == other.id

    override fun hashCode(): Int = id.hashCode()

}
