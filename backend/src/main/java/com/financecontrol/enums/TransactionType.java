package com.financecontrol.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {
    DEBIT(1, "debit"),
    CREDIT(2, "credit");

    private final Integer code;
    private final String value;

    TransactionType(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static TransactionType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransactionType type : TransactionType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid TransactionType code: " + code);
    }

    @JsonCreator
    public static TransactionType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("TransactionType value cannot be null");
        }
        for (TransactionType type : TransactionType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid TransactionType value: " + value);
    }
}
