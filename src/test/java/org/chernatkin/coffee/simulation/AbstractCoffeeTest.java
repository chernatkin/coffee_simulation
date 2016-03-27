package org.chernatkin.coffee.simulation;

import java.util.Map;
import java.util.Properties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedMap;

import org.chernatkin.coffee.simulation.service.CoffeeOrder;
import org.chernatkin.coffee.simulation.service.CoffeeType;
import org.chernatkin.coffee.simulation.service.OrderStep;
import org.chernatkin.coffee.simulation.service.PaymentType;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;

public abstract class AbstractCoffeeTest extends JerseyTest {

    protected static final int NUMBER_OF_MACHINES = 2;
    
    private Properties props;
    
    protected Properties getProperties(){
        if(props == null){
            props = new Properties();
            props.put("db.url", "jdbc:hsqldb:mem:coffeedb");
            props.put("db.login", "SA");
            props.put("db.pwd", "");
            props.put("db.pool.size", "2");
        }
        
        return props;
    }
    
    @Override
    protected Application configure() {
        return new ApplicationConfig(getProperties());
    }
    
    protected WebTarget coffeeAccountingTarget(){
        return target("/coffee/accounting");
    }
    
    protected WebTarget coffeeStatsTarget(){
        return target("/coffee/stats");
    }
    
    protected CoffeeOrder pickFavorites(CoffeeType coffeeType) {
        final CoffeeOrder order = coffeeAccountingTarget().path("pick_favorites")
                                                          .request()
                                                          .post(Entity.form(new Form("coffeeType", coffeeType.name())), 
                                                                CoffeeOrder.class);
        
        Assert.assertEquals(coffeeType, order.getCoffeeType());
        Assert.assertEquals(OrderStep.PICK_FAVORITES, order.getStep());
        Assert.assertNull(order.getPaymentType());
        Assert.assertNull(order.getMachineId());
        
        return order;
    }
    
    protected CoffeeOrder pay(PaymentType paymentType, long orderId) {
        final MultivaluedMap<String, String> params = new MultivaluedStringMap();
        params.putSingle("paymentType", paymentType.name());
        params.putSingle("orderId", Long.toString(orderId));
        
        CoffeeOrder payedOrder = coffeeAccountingTarget().path("pay")
                                                         .request()
                                                         .put(Entity.form(new Form(params)), 
                                                              CoffeeOrder.class);
        
        Assert.assertEquals(paymentType, payedOrder.getPaymentType());
        Assert.assertEquals(OrderStep.PAID, payedOrder.getStep());
        Assert.assertEquals(orderId, payedOrder.getOrderId());
        Assert.assertNull(payedOrder.getMachineId());
        
        return payedOrder;
    }
    
    protected CoffeeOrder assignMachine(CoffeeType coffeeType, long orderId) {
        final MultivaluedMap<String, String> params = new MultivaluedStringMap();
        params.putSingle("coffeeType", coffeeType.name());
        params.putSingle("orderId", Long.toString(orderId));
        
        CoffeeOrder order = coffeeAccountingTarget().path("/machine/assign")
                                                    .request()
                                                    .put(Entity.form(new Form(params)), 
                                                         CoffeeOrder.class);
        
        Assert.assertEquals(coffeeType, order.getCoffeeType());
        Assert.assertEquals(OrderStep.ASSIGNED_MACHINE, order.getStep());
        Assert.assertEquals(orderId, order.getOrderId());
        Assert.assertNotNull(order.getMachineId());
        
        return order;
    }
    
    protected CoffeeOrder coffeeDone(long orderId) {
        final MultivaluedMap<String, String> params = new MultivaluedStringMap();
        params.putSingle("orderId", Long.toString(orderId));
        
        CoffeeOrder order = coffeeAccountingTarget().path("/machine/done")
                                                    .request()
                                                    .put(Entity.form(new Form(params)), 
                                                         CoffeeOrder.class);
        
        Assert.assertEquals(OrderStep.COFFEE_DONE, order.getStep());
        Assert.assertEquals(orderId, order.getOrderId());
        Assert.assertNotNull(order.getMachineId());
        
        return order;
    }
    
    protected CoffeeOrder buyCoffeeFullFlow(final CoffeeType coffeeType, final PaymentType paymentType){
        CoffeeOrder order = pickFavorites(coffeeType);
        
        order = pay(paymentType, order.getOrderId());
        Assert.assertEquals(coffeeType, order.getCoffeeType());
        
        order = assignMachine(order.getCoffeeType(), order.getOrderId());
        
        CoffeeOrder doneOrder = coffeeDone(order.getOrderId());
        Assert.assertEquals(coffeeType, doneOrder.getCoffeeType());
        Assert.assertEquals(paymentType, doneOrder.getPaymentType());
        Assert.assertEquals(order.getMachineId(), doneOrder.getMachineId());
        
        return order;
    }
    
    protected Map<String, Integer> soldTotalStats() {
        
        Map<String, Integer> stats = coffeeStatsTarget().path("/sold/total")
                                                        .request()
                                                        .get(new GenericType<Map<String, Integer>>(){});
        
        
        
        Assert.assertEquals(stats.size(), PaymentType.values().length + 1);
        return stats;
    }
    
    protected Map<Integer, Map<String, Integer>> machineStats() {
        
        Map<Integer, Map<String, Integer>> stats = coffeeStatsTarget().path("/machine/total")
                                                                      .request()
                                                                      .get(new GenericType<Map<Integer, Map<String, Integer>>>(){});
        
        Assert.assertEquals(stats.size(), NUMBER_OF_MACHINES);
        for(Map<String, Integer> machineStat : stats.values()){
            Assert.assertEquals(machineStat.size(), CoffeeType.values().length + 1);
        }
        
        return stats;
    }
    
    protected int spendTimeAvg() {
        return coffeeStatsTarget().path("/spendtime/avg")
                                  .request()
                                  .get(Integer.class);
    }
    
    protected int spendTimeMax() {
        return coffeeStatsTarget().path("/spendtime/max")
                                  .request()
                                  .get(Integer.class);
    }
    
    protected int spendTimeMin() {
        return coffeeStatsTarget().path("/spendtime/min")
                                  .request()
                                  .get(Integer.class);
    }
}
