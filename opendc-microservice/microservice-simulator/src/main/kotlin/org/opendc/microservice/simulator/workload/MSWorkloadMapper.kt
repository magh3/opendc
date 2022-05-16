package org.opendc.microservice.simulator.workload

/**
 * A [MSWorkloadMapper] is responsible for mapping a MSInstance to a [MSWorkload] that
 * can be simulated.
 */
public interface MSWorkloadMapper {

    public fun createWorkload(): MSWorkload

}
