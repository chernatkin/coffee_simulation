package org.chernatkin.coffee.simulation.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class TooManyRequestsException extends WebApplicationException {

    public TooManyRequestsException() {
        super(Response.status(429).build());
    }
}
