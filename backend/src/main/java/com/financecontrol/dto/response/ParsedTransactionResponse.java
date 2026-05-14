package com.financecontrol.dto.response;

import com.financecontrol.enums.TransactionType;

public record ParsedTransactionResponse(
    String date,
    String description,
    double amount,
    TransactionType type,
    Long suggestedCategoryId,
    String suggestedCategoryName
) {}
