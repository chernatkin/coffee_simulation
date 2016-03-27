package org.chernatkin.coffee.simulation.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

public class CoffeeStatsService {
    
    @Inject
    private CoffeeDao coffeeDao;
    
    public Map<String, Integer> getSoldStatsByPaymentType() throws SQLException {
        
        final Map<String, Integer> stats = coffeeDao.getSoldStatsByPaymentType();
        int total = 0;
        for(PaymentType type : PaymentType.values()){
            total += Optional.ofNullable(stats.putIfAbsent(type.name(), 0))
                             .orElse(0);
        }
        
        stats.put("total", total);
        return stats;
    }
    
    public Map<Integer, Map<String, Integer>> getDispencedStatsByCoffeeType() throws SQLException {
        
        final Map<Integer, Map<String, Integer>> stats = coffeeDao.getDispencedStatsByCoffeeType();
        
        for(int machineId = 1; machineId <= CoffeeDao.MACHINES_COUNT; machineId++){
            int total = 0;
            Map<String, Integer> machineStats = stats.computeIfAbsent(machineId, k -> new HashMap<>());
            for(CoffeeType type : CoffeeType.values()){
                total += Optional.ofNullable(machineStats.putIfAbsent(type.name(), 0))
                                 .orElse(0);
            }
            machineStats.put("total", total);
        }
        
        return stats;
    }
    
    
    public int getAvgSpendTime() throws SQLException {
        return coffeeDao.getAvgSpendTime();
    }
    
    public int getMaxSpendTime() throws SQLException {
        return coffeeDao.getMaxSpendTime();
    }
    
    public int getMinSpendTime() throws SQLException {
        return coffeeDao.getMinSpendTime();
    }

}
