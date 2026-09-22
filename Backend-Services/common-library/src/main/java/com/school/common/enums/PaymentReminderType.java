package com.school.common.enums;

public enum PaymentReminderType {
	DUE_DATE_COMMING_TWO_DAUS("DUE_DATE_COMMING_TWO_DAUS"),
	DUE_DATE("DUE_DATE"),
	OVERDUE_7DAYS("OVERDUE_7DAYS"),
	OVERDUE_14DAYS("OVERDUE_14DAYS");

    private final String value;

    PaymentReminderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
