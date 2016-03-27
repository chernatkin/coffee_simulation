package org.chernatkin.coffee.simulation.service;

public enum CoffeeType {
    
    ESPRESSO(250),
    
    LATTE_MACCHIATO(500),
    
    CAPPUCCHINO(750);
    
    private final long time;
    
    private CoffeeType(long time){
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
