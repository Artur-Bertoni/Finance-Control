package com.financecontrol.service.finny.rules;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.service.finny.FinancialProfile;
import com.financecontrol.service.finny.TipCandidate;
import com.financecontrol.service.finny.TipRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Patrimônio caiu de forma relevante entre os dois últimos meses → sugere revisar gastos. */
@Component
public class BalanceDropRule implements TipRule {

    @Override
    public List<TipCandidate> evaluate(FinancialProfile p) {
        if (p.balanceDropPct() == null) return List.of();
        long pct = Math.round(p.balanceDropPct());
        return List.of(new TipCandidate("BALANCE_DROP", FinnyTipCategory.CASHFLOW, "warning", 70,
                Map.of("pct", pct)));
    }
}
