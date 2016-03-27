package org.chernatkin.coffee.simulation;

import org.chernatkin.coffee.simulation.service.CoffeeOrder;
import org.chernatkin.coffee.simulation.service.CoffeeType;
import org.chernatkin.coffee.simulation.service.PaymentType;
import org.junit.Assert;
import org.junit.Test;

public class CoffeeBuyTest extends AbstractCoffeeTest {
    
    @Test
    public void buyCoffeeSuccess() throws InterruptedException {
        
        for(PaymentType paymentType : PaymentType.values()){
            for(CoffeeType coffeeType : CoffeeType.values()){
                buyCoffeeFullFlow(coffeeType, paymentType);
                Thread.sleep(1000);
            }
        }
    }

}
