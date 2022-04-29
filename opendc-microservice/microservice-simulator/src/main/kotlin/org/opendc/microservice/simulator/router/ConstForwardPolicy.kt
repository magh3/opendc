package org.opendc.microservice.simulator.router


/**
 * forwardNr - number of services to forward request to.
 */
public class ConstForwardPolicy(private var forwardNr: Int): ForwardPolicy{


    public override fun getForwardNr(): Int {return forwardNr}



}
