package com.financecontrol.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BulkDeleteType {
    TRANSACTIONS("transactions"),
    BUDGETS("budgets"),
    GOALS("goals"),
    CATEGORIES("categories"),
    ACCOUNTS("accounts"),
    FINANCIAL_INSTITUTIONS("financial-institutions"),
    TRANSACTION_LOCALES("transaction-locales");

    private final String value;

    BulkDeleteType(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static BulkDeleteType fromValue(String value) {
        for (BulkDeleteType t : values()) {
            if (t.value.equals(value)) return t;
        }
        throw new IllegalArgumentException("Invalid BulkDeleteType: " + value);
    }
}
