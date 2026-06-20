package com.financecontrol.service.finny.rules;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.service.finny.FinancialProfile;
import com.financecontrol.service.finny.TipCandidate;
import com.financecontrol.service.finny.TipRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EngagementRule implements TipRule {

    @Override
    public List<TipCandidate> evaluate(FinancialProfile p) {
        if (p.hasData()) return List.of();
        return List.of(TipCandidate.of("NO_DATA", FinnyTipCategory.ENGAGEMENT, "info", 40));
    }
}
