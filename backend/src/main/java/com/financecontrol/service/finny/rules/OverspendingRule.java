package com.financecontrol.service.finny.rules;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.service.finny.FinancialProfile;
import com.financecontrol.service.finny.TipCandidate;
import com.financecontrol.service.finny.TipRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Gastos acima da renda no período → alerta com o % de excesso. */
@Component
public class OverspendingRule implements TipRule {

    @Override
    public List<TipCandidate> evaluate(FinancialProfile p) {
        if (!p.hasData() || p.totalIncome() <= 0 || p.totalExpenses() <= p.totalIncome()) return List.of();
        long pct = Math.round((p.totalExpenses() / p.totalIncome() - 1) * 100.0);
        return List.of(new TipCandidate("OVERSPENDING", FinnyTipCategory.BUDGET, "warning", 95,
                Map.of("pct", pct)));
    }
}
