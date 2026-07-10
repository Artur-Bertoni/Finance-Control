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
    List<CategorySuggestionDto> allSuggestedCategories,
    String installmentLabel,
    String invoiceReference
) {
    public ParsedTransactionResponse(String date, String description, double amount, TransactionType type,
                                     Long suggestedCategoryId, String suggestedCategoryName,
                                     boolean hasMultipleSuggestions, List<CategorySuggestionDto> allSuggestedCategories) {
        this(date, description, amount, type, suggestedCategoryId, suggestedCategoryName,
             hasMultipleSuggestions, allSuggestedCategories, null, null);
    }
}
