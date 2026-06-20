package com.financecontrol.service.finny;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.service.finny.rules.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FinnyRulesTest {

    private FinancialProfile profile(double income, double expenses, double balance,
                                     double avgMonthlyExpenses, Double balanceDrop,
                                     List<FinancialProfile.CategoryExpense> cats,
                                     List<FinancialProfile.GoalSnapshot> goals) {
        double net = income - expenses;
        double savingsPct = income > 0 ? (net / income) * 100.0 : 0.0;
        double emergency = avgMonthlyExpenses > 0 && balance > 0 ? balance / avgMonthlyExpenses : 0.0;
        boolean hasData = income > 0 || expenses > 0;
        return new FinancialProfile(hasData, 6, income, expenses, net, savingsPct,
                balance, avgMonthlyExpenses, emergency, balanceDrop, cats, goals);
    }

    @Test
    void engagement_semDados_emiteNoData() {
        FinancialProfile p = profile(0, 0, 0, 0, null, List.of(), List.of());
        List<TipCandidate> tips = new EngagementRule().evaluate(p);
        assertThat(tips).singleElement()
                .satisfies(t -> assertThat(t.ruleKey()).isEqualTo("NO_DATA"));
    }

    @Test
    void overspending_gastosAcimaDaRenda_emiteComPct() {
        FinancialProfile p = profile(1000, 1200, 0, 200, null, List.of(), List.of());
        List<TipCandidate> tips = new OverspendingRule().evaluate(p);
        assertThat(tips).singleElement().satisfies(t -> {
            assertThat(t.ruleKey()).isEqualTo("OVERSPENDING");
            assertThat(t.category()).isEqualTo(FinnyTipCategory.BUDGET);
            assertThat(t.params()).containsEntry("pct", 20L);
        });
    }

    @Test
    void savingsRate_baixa_emiteLow() {
        FinancialProfile p = profile(1000, 950, 0, 950, null, List.of(), List.of());
        assertThat(new SavingsRateRule().evaluate(p))
                .singleElement().satisfies(t -> assertThat(t.ruleKey()).isEqualTo("SAVINGS_RATE_LOW"));
    }

    @Test
    void savingsRate_boa_emiteGood() {
        FinancialProfile p = profile(1000, 700, 0, 700, null, List.of(), List.of());
        assertThat(new SavingsRateRule().evaluate(p))
                .singleElement().satisfies(t -> assertThat(t.ruleKey()).isEqualTo("SAVINGS_RATE_GOOD"));
    }

    @Test
    void topCategory_emiteComCategoriaEId() {
        var cat = new FinancialProfile.CategoryExpense(9L, "Restaurantes", 300, 60);
        FinancialProfile p = profile(1000, 500, 0, 500, null, List.of(cat), List.of());
        assertThat(new TopCategoryRule().evaluate(p)).singleElement().satisfies(t -> {
            assertThat(t.ruleKey()).isEqualTo("TOP_CATEGORY");
            assertThat(t.params()).containsEntry("category", "Restaurantes").containsEntry("categoryId", 9L);
        });
    }

    @Test
    void balanceDrop_quedaRelevante_emite() {
        FinancialProfile p = profile(1000, 500, 0, 500, 12.0, List.of(), List.of());
        assertThat(new BalanceDropRule().evaluate(p))
                .singleElement().satisfies(t -> assertThat(t.params()).containsEntry("pct", 12L));
    }

    @Test
    void emergencyFund_baixa_emiteLow() {
        FinancialProfile p = profile(1000, 500, 500, 500, null, List.of(), List.of());
        assertThat(new EmergencyFundRule().evaluate(p)).singleElement().satisfies(t -> {
            assertThat(t.ruleKey()).isEqualTo("EMERGENCY_FUND_LOW");
            assertThat(t.category()).isEqualTo(FinnyTipCategory.SAVINGS);
        });
    }

    @Test
    void emergencyFund_robustaComSobra_emiteInvestimentoEducacional() {
        FinancialProfile p = profile(2000, 1000, 7000, 1000, null, List.of(), List.of());
        assertThat(new EmergencyFundRule().evaluate(p)).singleElement().satisfies(t -> {
            assertThat(t.ruleKey()).isEqualTo("EMERGENCY_FUND_READY");
            assertThat(t.category()).isEqualTo(FinnyTipCategory.INVESTMENT);
        });
    }

    @Test
    void emergencyFund_zonaSaudavel_naoEmite() {
        FinancialProfile p = profile(2000, 1000, 4000, 1000, null, List.of(), List.of());
        assertThat(new EmergencyFundRule().evaluate(p)).isEmpty();
    }

    @Test
    void goalProgress_limiteDeGastoQuaseEstourado_emite90() {
        var goal = new FinancialProfile.GoalSnapshot(1L, "Lazer", "EXPENSE_LIMIT", 92, null);
        FinancialProfile p = profile(1000, 500, 0, 500, null, List.of(), List.of(goal));
        assertThat(new GoalProgressRule().evaluate(p))
                .anySatisfy(t -> assertThat(t.ruleKey()).isEqualTo("GOAL_EXPENSE_90"));
    }

    @Test
    void goalProgress_prazoProximo_emiteDeadline() {
        var goal = new FinancialProfile.GoalSnapshot(2L, "Viagem", "SAVINGS", 20, LocalDate.now().plusDays(3));
        FinancialProfile p = profile(1000, 500, 0, 500, null, List.of(), List.of(goal));
        assertThat(new GoalProgressRule().evaluate(p))
                .anySatisfy(t -> {
                    assertThat(t.ruleKey()).isEqualTo("GOAL_DEADLINE");
                    assertThat(t.params()).containsKey("days");
                });
    }
}
