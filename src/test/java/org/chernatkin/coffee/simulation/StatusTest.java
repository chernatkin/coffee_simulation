package org.chernatkin.coffee.simulation;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

public class StatusTest extends AbstractCoffeeTest {
    
    @Test
    public void statusTest(){
        final Response ok = coffeeAccountingTarget().path("/status").request().get();
        
        Assert.assertEquals(Response.Status.OK.getStatusCode(), ok.getStatus());
        Assert.assertEquals("OK", ok.readEntity(String.class));
    }
}
