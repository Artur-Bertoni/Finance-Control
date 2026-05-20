package com.financecontrol.dto.response;

public record FieldChangeResponse(
    String fieldName,
    String oldValue,
    String newValue
) {}
