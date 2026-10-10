package com.chococraft.enums;

public enum PaymentMethod {
    COD("Cash on Delivery"),
    MOCK_ONLINE("Mock Online Payment (UPI / Card / NetBanking)");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
