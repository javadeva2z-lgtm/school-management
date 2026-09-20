package com.school.common.enums;

public enum TeacherClassLevel {
    PRE_PRIMARY("PRE_PRIMARY"),
    PRIMARY("PRIMARY"),
    UPPER_PRIMARY("UPPER_PRIMARY"),
    SECONDARY("SECONDARY"),
    HIGHER_SECONDARY("HIGHER_SECONDARY"),
    COMMON("COMMON");

    private final String value;

    TeacherClassLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
