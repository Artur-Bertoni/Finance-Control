package com.financecontrol.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GoalType {
    EXPENSE_LIMIT("expense_limit"),
    SAVINGS("savings"),
    INCOME("income");

    private final String value;

    GoalType(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static GoalType fromValue(String value) {
        for (GoalType t : values()) {
            if (t.value.equals(value)) return t;
        }
        throw new IllegalArgumentException("Invalid GoalType: " + value);
    }
}
