package com.financecontrol.dto.response;

public record BudgetResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconKey,
        Double monthlyLimit,
        Double spent,
        Double percent
) {}
