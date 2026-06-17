package com.financecontrol.service.finny;

import com.financecontrol.enums.FinnyTipCategory;

import java.util.Map;

/**
 * Uma dica candidata emitida por uma regra, antes de ranquear/persistir.
 * O {@code baseScore} é a relevância "crua" da regra; o agente multiplica pelo peso
 * adaptativo da categoria para chegar ao score final.
 *
 * @param params valores que personalizam o texto (ex: {"pct":15}). Renderizados no front via i18n.
 */
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
