package org.chernatkin.coffee.simulation.service;

import java.sql.SQLException;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/coffee/stats")
public class CoffeeStatsResource {
    
    @Inject
    private CoffeeStatsService coffeeStatsService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/sold/total")
    public Map<String, Integer> getSoldStats() throws SQLException {
        return coffeeStatsService.getSoldStatsByPaymentType();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/machine/total")
    public Map<Integer, Map<String, Integer>> getDispencedStats() throws SQLException {
        return coffeeStatsService.getDispencedStatsByCoffeeType();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spendtime/avg")
    public int getAvgSpendTime() throws SQLException {
        return coffeeStatsService.getAvgSpendTime();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spendtime/max")
    public int getMaxSpendTime() throws SQLException {
        return coffeeStatsService.getMaxSpendTime();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spendtime/min")
    public int getMinSpendTime() throws SQLException {
        return coffeeStatsService.getMinSpendTime();
    }
}
