package com.school.common.enums;

public enum UserRoleWithRolePrefix {
    ADMIN("ROLE_ADMIN"),
    TEACHER("ROLE_TEACHER"),
    STUDENT("ROLE_STUDENT"),
    SUPER_ADMIN("ROLE_SUPER_ADMIN");

    private final String value;

    UserRoleWithRolePrefix(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
