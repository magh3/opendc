package org.opendc.microservice.simulator.trace

public class MSTraceRow(

    public var instanceId: String,

    public var endTime: Float,

    public var exeTime: Float,

    public var startTime: Float

    ){

    override fun toString(): String {
        return " $instanceId, $endTime, $exeTime, $startTime \n"
    }

}
