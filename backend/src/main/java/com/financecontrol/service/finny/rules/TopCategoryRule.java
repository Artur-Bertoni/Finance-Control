package com.financecontrol.service.finny.rules;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.service.finny.FinancialProfile;
import com.financecontrol.service.finny.TipCandidate;
import com.financecontrol.service.finny.TipRule;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maior categoria de despesa → sugere revisão.
 * Guarda também o categoryId nos params (oculto na renderização) para o
 * aprendizado implícito poder medir, depois, se o gasto nessa categoria caiu.
 */
@Component
public class TopCategoryRule implements TipRule {

    @Override
    public List<TipCandidate> evaluate(FinancialProfile p) {
        if (p.topExpenseCategories().isEmpty()) return List.of();
        FinancialProfile.CategoryExpense top = p.topExpenseCategories().get(0);
        if (top.name() == null || top.name().isBlank()) return List.of();

        Map<String, Object> params = new HashMap<>();
        params.put("category", top.name());
        params.put("pct", Math.round(top.sharePct()));
        if (top.categoryId() != null) params.put("categoryId", top.categoryId());

        return List.of(new TipCandidate("TOP_CATEGORY", FinnyTipCategory.BUDGET, "info", 65, params));
    }
}
