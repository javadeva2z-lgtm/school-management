package com.school.common.enums;

public enum PaymentStatus {
    PENDING("PENDING"),
    PARTIAL("PARTIAL"),
    PAID("PAID"),
    EXEMPT("EXEMPT"),
    OVERDUE("OVERDUE"),
    CANCELLED("CANCELLED");
	
    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
