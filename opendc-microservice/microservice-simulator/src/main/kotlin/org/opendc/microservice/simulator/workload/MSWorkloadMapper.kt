package org.opendc.microservice.simulator.workload

import org.opendc.microservice.simulator.microservice.MSInstance

/**
 * A [MSWorkloadMapper] is responsible for mapping a MSInstance to a [MSWorkload] that
 * can be simulated.
 */
public interface MSWorkloadMapper {

    public fun createWorkload(msInstance: MSInstance): MSWorkload

}
