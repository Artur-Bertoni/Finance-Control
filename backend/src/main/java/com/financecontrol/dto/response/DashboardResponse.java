package com.financecontrol.dto.response;

import java.util.List;

public record DashboardResponse(
        List<MonthlyDataPoint> monthlyData,
        List<CategoryDataPoint> categoryExpenses,
        List<CategoryDataPoint> categoryIncomes,
        List<WealthDataPoint> wealthEvolution
) {
    public record MonthlyDataPoint(String month, Double income, Double expenses) {}
    public record CategoryDataPoint(Long categoryId, String categoryName, Double total) {}
    public record WealthDataPoint(String month, Double balance) {}
}
