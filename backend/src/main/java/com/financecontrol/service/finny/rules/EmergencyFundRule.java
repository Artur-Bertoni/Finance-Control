package com.financecontrol.service.finny.rules;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.service.finny.FinancialProfile;
import com.financecontrol.service.finny.TipCandidate;
import com.financecontrol.service.finny.TipRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EmergencyFundRule implements TipRule {

    @Override
    public List<TipCandidate> evaluate(FinancialProfile p) {
        if (!p.hasData() || p.avgMonthlyExpenses() <= 0) return List.of();
        double months = p.emergencyFundMonths();
        double rounded = Math.round(months * 10.0) / 10.0;

        if (months < 3.0) {
            return List.of(new TipCandidate("EMERGENCY_FUND_LOW", FinnyTipCategory.SAVINGS, "warning", 85,
                    Map.of("months", rounded)));
        }
        if (months >= 6.0 && p.net() > 0) {
            return List.of(new TipCandidate("EMERGENCY_FUND_READY", FinnyTipCategory.INVESTMENT, "success", 75,
                    Map.of("months", rounded)));
        }
        return List.of();
    }
}
