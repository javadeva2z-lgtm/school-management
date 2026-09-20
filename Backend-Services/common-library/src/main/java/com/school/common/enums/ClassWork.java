package com.school.common.enums;

public enum ClassWork {
    CLASSWORK("CLASSWORK"),
    HOMEWORK("HOMEWORK");

    private final String value;

    ClassWork(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
