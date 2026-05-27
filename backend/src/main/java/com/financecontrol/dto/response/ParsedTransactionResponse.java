package com.financecontrol.dto.response;

import com.financecontrol.enums.TransactionType;

import java.util.List;

public record ParsedTransactionResponse(
    String date,
    String description,
    double amount,
    TransactionType type,
    Long suggestedCategoryId,
    String suggestedCategoryName,
    boolean hasMultipleSuggestions,
    List<CategorySuggestionDto> allSuggestedCategories
) {}
