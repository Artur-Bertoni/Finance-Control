package com.financecontrol.service.finny.rules;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.service.finny.FinancialProfile;
import com.financecontrol.service.finny.TipCandidate;
import com.financecontrol.service.finny.TipRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SavingsRateRule implements TipRule {

    @Override
    public List<TipCandidate> evaluate(FinancialProfile p) {
        if (p.totalIncome() <= 0 || p.net() <= 0) return List.of();
        long pct = Math.round(p.savingsRatePct());
        Map<String, Object> params = Map.of("pct", pct);

        if (pct < 10) return List.of(new TipCandidate("SAVINGS_RATE_LOW",    FinnyTipCategory.SAVINGS, "warning", 80, params));
        if (pct < 20) return List.of(new TipCandidate("SAVINGS_RATE_MEDIUM", FinnyTipCategory.SAVINGS, "info",    60, params));
        return List.of(new TipCandidate("SAVINGS_RATE_GOOD",   FinnyTipCategory.SAVINGS, "success", 50, params));
    }
}
