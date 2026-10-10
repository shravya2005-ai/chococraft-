package com.chococraft.enums;

public enum OrderStatus {
    PLACED("Order Placed"),
    CONFIRMED("Confirmed"),
    PREPARING("Preparing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
