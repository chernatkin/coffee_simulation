package org.chernatkin.coffee.simulation.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class OrderNotFoundException extends WebApplicationException {

    public OrderNotFoundException() {
        super(Response.status(Status.NOT_FOUND).build());
    }

}
