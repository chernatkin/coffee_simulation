package org.chernatkin.coffee.simulation;

import java.util.HashMap;
import java.util.Map;

import org.chernatkin.coffee.simulation.service.CoffeeOrder;
import org.chernatkin.coffee.simulation.service.CoffeeType;
import org.chernatkin.coffee.simulation.service.PaymentType;
import org.junit.Assert;
import org.junit.Test;

public class CoffeeStatsTest extends AbstractCoffeeTest {

    @Test
    public void soldTotalStatsTest() throws InterruptedException {
        
        final Map<String, Integer> soldStatsExpected = new HashMap<>();
        
        checkStatsMap(soldStatsExpected, soldTotalStats(), PaymentType.values());
        
        for(PaymentType paymentType : PaymentType.values()){
            for(CoffeeType coffeeType : CoffeeType.values()){
                buyCoffeeFullFlow(coffeeType, paymentType);
                
                soldStatsExpected.compute(paymentType.name(), (k, v) -> v == null ? 1 : v + 1);
                soldStatsExpected.compute("total", (k, v) -> v == null ? 1 : v + 1);
                
                checkStatsMap(soldStatsExpected, soldTotalStats(), PaymentType.values());
                
                Thread.sleep(1000);
            }
        }
    }
    
    @Test
    public void machineStatsTest() throws InterruptedException {
        
        final Map<Integer, Map<String, Integer>> machineStatsExpected = new HashMap<>();
        
        checkMapOfStats(machineStatsExpected, machineStats(), CoffeeType.values());
        
        for(PaymentType paymentType : PaymentType.values()){
            for(CoffeeType coffeeType : CoffeeType.values()){
                final CoffeeOrder order = buyCoffeeFullFlow(coffeeType, paymentType);
                
                machineStatsExpected.get(order.getMachineId()).compute(coffeeType.name(), (k, v) -> v == null ? 1 : v + 1);
                machineStatsExpected.get(order.getMachineId()).compute("total", (k, v) -> v == null ? 1 : v + 1);
                
                checkMapOfStats(machineStatsExpected, machineStats(), CoffeeType.values());
                
                Thread.sleep(1000);
            }
        }
    }
    
    @Test
    public void spendTimeTest() throws InterruptedException {
        
        for(PaymentType paymentType : PaymentType.values()){
            for(CoffeeType coffeeType : CoffeeType.values()){
                buyCoffeeFullFlow(coffeeType, paymentType);
                Thread.sleep(1000);
            }
        }
        
        Assert.assertEquals(2375, spendTimeAvg());
        Assert.assertEquals(2000, spendTimeMin());
        Assert.assertEquals(2750, spendTimeMax());
    }
    
    private void checkMapOfStats(Map<Integer, Map<String, Integer>> expected, Map<Integer, Map<String, Integer>> actual, Enum[] values){
        for(int machineId = 1; machineId <= NUMBER_OF_MACHINES; machineId++){
            checkStatsMap(expected.computeIfAbsent(machineId, k -> new HashMap<>()), actual.get(machineId), values);
        }
    }
    
    private void checkStatsMap(Map<String, Integer> expected, Map<String, Integer> actual, Enum[] values){
        for(Enum type : values){
            Assert.assertEquals(expected.computeIfAbsent(type.name(), k -> 0), actual.get(type.name()));
        }
        
        Assert.assertEquals(expected.computeIfAbsent("total", k -> 0), actual.get("total"));
    }
}
