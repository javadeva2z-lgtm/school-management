package com.school.common.enums;

public enum UserRole {
    ADMIN("ADMIN"),
    TEACHER("TEACHER"),
    STUDENT("STUDENT"),
    PARENT("PARENT");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
