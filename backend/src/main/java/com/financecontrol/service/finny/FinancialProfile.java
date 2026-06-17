package com.financecontrol.service.finny;

import java.time.LocalDate;
import java.util.List;

/**
 * Retrato imutável da situação financeira do usuário numa janela recente.
 * É a ÚNICA entrada das regras (advisors): elas leem este objeto e decidem se emitem dica.
 * Manter as regras dependentes só do perfil (e não do banco) as deixa puras e fáceis de testar.
 */
public record FinancialProfile(
        boolean hasData,
        int monthsAnalyzed,
        double totalIncome,
        double totalExpenses,
        double net,
        /** net / renda * 100; 0 quando não há renda no período. */
        double savingsRatePct,
        double currentBalance,
        double avgMonthlyExpenses,
        /** Quantos meses de despesa média o saldo atual cobre. */
        double emergencyFundMonths,
        /** Queda % do patrimônio entre os dois últimos meses, ou null se não caiu. */
        Double balanceDropPct,
        /** Categorias de despesa ordenadas da maior para a menor. */
        List<CategoryExpense> topExpenseCategories,
        List<GoalSnapshot> activeGoals
) {
    public record CategoryExpense(Long categoryId, String name, double total, double sharePct) {}

    public record GoalSnapshot(Long id, String name, String type, double progressPct, LocalDate endDate) {}
}
