package org.chernatkin.coffee.simulation.service;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/coffee/accounting")
public class CoffeeAccountResource {
    
    @Inject
    private CoffeeService coffeeService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/status")
    public String status() {
        return "OK";
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/type/all")
    public List<CoffeeType> getCoffeeTypes() {
        return Arrays.asList(CoffeeType.values());
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pick_favorites")
    public CoffeeOrder pickFavorites(@FormParam("coffeeType") final CoffeeType coffeeType) throws SQLException {
        return coffeeService.pickFavorites(checkNotNull(coffeeType));
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pay")
    public CoffeeOrder pay(@FormParam("orderId") final long orderId, @FormParam("paymentType") final PaymentType paymentType) throws SQLException {
        return coffeeService.payCoffee(checkOrderId(orderId), checkNotNull(paymentType));
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/machine/assign")
    public CoffeeOrder assignMachine(@FormParam("orderId") final long orderId, @FormParam("coffeeType") final CoffeeType coffeeType) throws SQLException {
        return coffeeService.assignMachine(checkOrderId(orderId), checkNotNull(coffeeType));
    }
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/machine/done")
    public CoffeeOrder coffeeDone(@FormParam("orderId") final long orderId) throws SQLException {
        return coffeeService.coffeeDone(checkOrderId(orderId));
    }
    
    private long checkOrderId(final long orderId){
        if(orderId < 0){
            throw new IllegalArgumentException("Order id must not be less than zero");
        }
        return orderId;
    }
    
    private <T> T checkNotNull(T value) {
        return Optional.ofNullable(value)
                       .orElseThrow(() -> new IllegalArgumentException());
    }
}
