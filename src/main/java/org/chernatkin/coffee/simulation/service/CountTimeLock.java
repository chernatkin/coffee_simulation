package org.chernatkin.coffee.simulation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CountTimeLock {

    private final int maxCount;
    
    private final Map<Integer, Long> slots;
    
    private final List<Integer> slotNumbers = new ArrayList<>();

    public CountTimeLock(final int maxCount) {
        this.maxCount = maxCount;
        this.slots = new HashMap<>();
        for(int i = 1; i <= maxCount; i++){
            slots.put(i, null);
            slotNumbers.add(i);
        }
    }
    
    public synchronized int tryLock(final long time, final TimeUnit timeUnit){
        final long timeout = timeUnit.toMillis(time);
        final long current = System.currentTimeMillis();
        
        for(Integer slot : slotNumbers){
            final Long oldValue = slots.get(slot);
            if(oldValue == null || current >= oldValue.longValue()){
                slots.put(slot, current + timeout);
                return slot;
            }
        }
        
        return -1;
    }
}
