package com.financecontrol.service.finny;

import com.financecontrol.dto.response.DashboardResponse;
import com.financecontrol.entity.Goal;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.GoalRepository;
import com.financecontrol.service.GoalService;
import com.financecontrol.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Monta o {@link FinancialProfile} a partir dos dados reais do usuário.
 * Reaproveita o {@link ReportService} (mesmas agregações do dashboard) e o {@link GoalService}
 * (mesmo cálculo de progresso de metas), garantindo que o agente "enxergue" os mesmos números
 * que o usuário vê nos gráficos.
 */
@Service
@RequiredArgsConstructor
public class FinancialProfileService {

    /** Janela de análise: mês atual + 5 anteriores. */
    private static final int WINDOW_MONTHS = 6;

    private final ReportService reportService;
    private final GoalService goalService;
    private final GoalRepository goalRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public FinancialProfile build(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusMonths(WINDOW_MONTHS - 1L).withDayOfMonth(1);

        DashboardResponse d = reportService.getDashboard(userId, start, today, null);

        double totalIncome   = d.monthlyData().stream().mapToDouble(m -> nz(m.income())).sum();
        double totalExpenses = d.monthlyData().stream().mapToDouble(m -> nz(m.expenses())).sum();
        double net           = totalIncome - totalExpenses;

        int monthsAnalyzed       = Math.max(1, d.monthlyData().size());
        double avgMonthlyExpenses = totalExpenses / monthsAnalyzed;

        double savingsRatePct = totalIncome > 0 ? (net / totalIncome) * 100.0 : 0.0;

        double currentBalance = nz(accountRepository.sumBalance(userId, null));
        double emergencyFundMonths = avgMonthlyExpenses > 0 && currentBalance > 0
                ? currentBalance / avgMonthlyExpenses
                : 0.0;

        Double balanceDropPct = computeBalanceDrop(d.balanceEvolution());

        List<FinancialProfile.CategoryExpense> topCategories = new ArrayList<>();
        for (DashboardResponse.CategoryDataPoint c : d.categoryExpenses()) {
            double share = totalExpenses > 0 ? (c.total() / totalExpenses) * 100.0 : 0.0;
            topCategories.add(new FinancialProfile.CategoryExpense(c.categoryId(), c.categoryName(), c.total(), share));
        }

        List<FinancialProfile.GoalSnapshot> goals = new ArrayList<>();
        for (Goal g : goalRepository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE)) {
            double target = g.getTargetAmount() != null ? g.getTargetAmount() : 0.0;
            double progress = target > 0 ? (goalService.calculateCurrentAmount(g) / target) * 100.0 : 0.0;
            goals.add(new FinancialProfile.GoalSnapshot(
                    g.getId(), g.getName(), g.getType() != null ? g.getType().name() : null, progress, g.getEndDate()));
        }

        boolean hasData = totalIncome > 0 || totalExpenses > 0;

        return new FinancialProfile(hasData, monthsAnalyzed, totalIncome, totalExpenses, net,
                savingsRatePct, currentBalance, avgMonthlyExpenses, emergencyFundMonths,
                balanceDropPct, topCategories, goals);
    }

    /** Queda % entre os dois últimos pontos de patrimônio (>= 5% para evitar ruído). */
    private Double computeBalanceDrop(List<DashboardResponse.WealthDataPoint> balances) {
        if (balances == null || balances.size() < 2) return null;
        double last = nz(balances.get(balances.size() - 1).balance());
        double prev = nz(balances.get(balances.size() - 2).balance());
        if (prev <= 0 || last >= prev) return null;
        double dropPct = ((prev - last) / prev) * 100.0;
        return dropPct >= 5.0 ? dropPct : null;
    }

    private static double nz(Double v) {
        return v != null ? v : 0.0;
    }
}
