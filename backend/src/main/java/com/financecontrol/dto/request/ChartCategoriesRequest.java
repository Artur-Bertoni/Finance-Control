package com.financecontrol.dto.request;

import java.util.List;

public record ChartCategoriesRequest(
    List<Long> expensePinned,
    List<Long> expenseGrouped,
    List<Long> incomePinned,
    List<Long> incomeGrouped
) {}
