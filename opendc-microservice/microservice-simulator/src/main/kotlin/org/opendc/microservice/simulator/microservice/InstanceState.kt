package org.opendc.microservice.simulator.microservice

/**
 * This enumeration describes the states of a [MSInstance].
 */
public enum class InstanceState {

    /**
     * The ms instance is idle and ready to execute.
     */
    Idle,

    /**
     * The function instance is executing.
     */
    Active,

    /**
     * The function instance is released and cannot be used anymore.
     */
    Deleted
}
