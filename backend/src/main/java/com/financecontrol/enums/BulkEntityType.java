package com.financecontrol.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BulkEntityType {
    TRANSACTIONS("transactions"),
    BUDGETS("budgets"),
    GOALS("goals"),
    CATEGORIES("categories"),
    ACCOUNTS("accounts"),
    FINANCIAL_INSTITUTIONS("financial-institutions"),
    TRANSACTION_LOCALES("transaction-locales");

    private final String value;

    BulkEntityType(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static BulkEntityType fromValue(String value) {
        for (BulkEntityType t : values()) {
            if (t.value.equals(value)) return t;
        }
        throw new IllegalArgumentException("Invalid BulkEntityType: " + value);
    }
}
