package org.chernatkin.coffee.simulation.service;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.chernatkin.coffee.simulation.exception.TooManyRequestsException;

public class CoffeeService {
    
    private static final long PICK_FAVORITE_TIME_MILLIS = 500;
    
    private static final CountTimeLock pickFavoritesLock = new CountTimeLock(10);
    
    private static final CountTimeLock payLock = new CountTimeLock(5);
    
    private static final CountTimeLock getCoffeeLock = new CountTimeLock(2);
    
    @Inject
    private CoffeeDao coffeeDao;
    
    public CoffeeOrder pickFavorites(final CoffeeType coffeeType) throws SQLException {
        lockOrThrow(pickFavoritesLock, PICK_FAVORITE_TIME_MILLIS);
        return coffeeDao.pickFavorites(coffeeType);
    }
    
    public CoffeeOrder payCoffee(final long orderId, final PaymentType paymentType) throws SQLException {
        lockOrThrow(payLock, paymentType.getTime());
        return coffeeDao.payCoffee(orderId, paymentType);
    }
    
    public CoffeeOrder assignMachine(final long orderId, final CoffeeType coffeeType) throws SQLException {
        final CoffeeOrder order = coffeeDao.loadOrder(orderId);
        if(!order.getStep().equals(OrderStep.PAID)){
            throw new IllegalArgumentException("Order should be paid");
        }
        return coffeeDao.assignMachine(order, lockOrThrow(getCoffeeLock, 1 + coffeeType.getTime()));
    }
    
    public CoffeeOrder coffeeDone(final long orderId) throws SQLException{
        final CoffeeOrder order = coffeeDao.loadOrder(orderId);
        return coffeeDao.coffeeDone(order);
    }
    
    private int lockOrThrow(final CountTimeLock countLock, final long time){
        final int slot = countLock.tryLock(time, TimeUnit.MILLISECONDS);
        if(slot == -1){
            throw new TooManyRequestsException();
        }
        return slot;
    }
}
