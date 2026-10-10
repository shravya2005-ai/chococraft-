package com.chococraft.enums;

public enum BoxSize {
    SMALL("Small Gift Box", 4, 150.00),
    MEDIUM("Medium Gift Box", 8, 250.00),
    LARGE("Large Luxury Hamper Box", 12, 350.00);

    private final String displayName;
    private final int capacity;
    private final double baseBoxPrice;

    BoxSize(String displayName, int capacity, double baseBoxPrice) {
        this.displayName = displayName;
        this.capacity = capacity;
        this.baseBoxPrice = baseBoxPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getBaseBoxPrice() {
        return baseBoxPrice;
    }
}
