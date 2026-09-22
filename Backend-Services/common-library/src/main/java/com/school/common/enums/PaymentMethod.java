package com.school.common.enums;

public enum PaymentMethod {
	//CASH, CARD, UPI, BANK_TRANSFER, CHEQUE
    CARD("CARD"),
    CASH("CASH"),
    UPI("UPI"),
    BANK_TRANSFER("BANK_TRANSFER"),
    CHEQUE("CHEQUE");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
