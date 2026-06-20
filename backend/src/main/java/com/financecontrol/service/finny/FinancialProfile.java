package com.financecontrol.service.finny;

import java.time.LocalDate;
import java.util.List;

public record FinancialProfile(
        boolean hasData,
        int monthsAnalyzed,
        double totalIncome,
        double totalExpenses,
        double net,
        double savingsRatePct,
        double currentBalance,
        double avgMonthlyExpenses,
        double emergencyFundMonths,
        Double balanceDropPct,
        List<CategoryExpense> topExpenseCategories,
        List<GoalSnapshot> activeGoals
) {
    public record CategoryExpense(Long categoryId, String name, double total, double sharePct) {}

    public record GoalSnapshot(Long id, String name, String type, double progressPct, LocalDate endDate) {}
}
