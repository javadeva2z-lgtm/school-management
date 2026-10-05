package com.school.common.enums;

public enum UserRole {
    ADMIN("ADMIN"),
    MANAGER("MANAGER"),
    TEACHER("TEACHER"),
    STUDENT("STUDENT"),
	SUPER_ADMIN("SUPER_ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
