package com.school.common.enums;

public enum AttendanceStatus {
    PRESENT("PRESENT"),
    ABSENT("ABSENT"),
    LEAVE("LEAVE"),
    SICK_LEAVE("SICK_LEAVE");

    private final String value;

    AttendanceStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
