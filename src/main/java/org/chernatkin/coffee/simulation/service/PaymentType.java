package org.chernatkin.coffee.simulation.service;

public enum PaymentType {
    
    CASH(500),
    
    CARD(250);
    
    private final long time;
    
    private PaymentType(long time){
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public static PaymentType valueOfNullable(String name){
        for(PaymentType type : PaymentType.values()){
            if(type.name().equals(name)){
                return type;
            }
        }
        return null;
    }
}
