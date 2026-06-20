package com.financecontrol.service.finny;

import com.financecontrol.enums.FinnyTipCategory;

import java.util.Map;

public record TipCandidate(
        String ruleKey,
        FinnyTipCategory category,
        String severity,
        double baseScore,
        Map<String, Object> params
) {
    public static TipCandidate of(String ruleKey, FinnyTipCategory category, String severity, double baseScore) {
        return new TipCandidate(ruleKey, category, severity, baseScore, Map.of());
    }
}
