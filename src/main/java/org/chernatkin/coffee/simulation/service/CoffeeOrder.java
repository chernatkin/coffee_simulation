package org.chernatkin.coffee.simulation.service;

public class CoffeeOrder {
    
    private long orderId;
    
    private CoffeeType coffeeType;
    
    private PaymentType paymentType;
    
    private OrderStep step;
    
    private Integer machineId;

    public CoffeeOrder() {
    }

    public CoffeeOrder(long orderId, CoffeeType coffeeType, PaymentType paymentType, OrderStep step, Integer machineId) {
        this.orderId = orderId;
        this.coffeeType = coffeeType;
        this.paymentType = paymentType;
        this.step = step;
        this.machineId = machineId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

    public void setCoffeeType(CoffeeType coffeeType) {
        this.coffeeType = coffeeType;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public OrderStep getStep() {
        return step;
    }

    public void setStep(OrderStep step) {
        this.step = step;
    }
    
    public Integer getMachineId() {
        return machineId;
    }

    public void setMachineId(Integer machineId) {
        this.machineId = machineId;
    }

    @Override
    public String toString() {
        return "CoffeeOrder [orderId=" + orderId + ", coffeeType=" + coffeeType
                + ", paymentType=" + paymentType + ", step=" + step
                + ", machineId=" + machineId + "]";
    }
}
