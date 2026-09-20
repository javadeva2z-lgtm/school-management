package com.school.common.enums;

public enum LeaveStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    WITHDRAWN("WITHDRAWN");

    private final String value;

    LeaveStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
