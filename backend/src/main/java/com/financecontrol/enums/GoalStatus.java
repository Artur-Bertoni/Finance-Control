package com.financecontrol.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GoalStatus {
    ACTIVE("active"),
    COMPLETED("completed"),
    EXPIRED("expired"),
    ARCHIVED("archived");

    private final String value;

    GoalStatus(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static GoalStatus fromValue(String value) {
        for (GoalStatus s : values()) {
            if (s.value.equals(value)) return s;
        }
        throw new IllegalArgumentException("Invalid GoalStatus: " + value);
    }
}
