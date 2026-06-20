package com.financecontrol.dto.request;

public record BudgetRequest(
        Long categoryId,
        Double monthlyLimit
) {}
