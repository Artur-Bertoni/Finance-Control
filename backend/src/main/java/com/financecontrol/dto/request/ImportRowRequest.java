package com.financecontrol.dto.request;

import com.financecontrol.enums.TransactionType;

public record ImportRowRequest(
    String date,
    String description,
    double amount,
    TransactionType type,
    Long categoryId,
    Long localeId,
    boolean skip
) {}
