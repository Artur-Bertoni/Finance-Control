package com.financecontrol.service.finny.rules;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.service.finny.FinancialProfile;
import com.financecontrol.service.finny.TipCandidate;
import com.financecontrol.service.finny.TipRule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GoalProgressRule implements TipRule {

    private static final int MAX_GOAL_TIPS = 3;

    @Override
    public List<TipCandidate> evaluate(FinancialProfile p) {
        List<TipCandidate> out = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (FinancialProfile.GoalSnapshot g : p.activeGoals()) {
            if (out.size() >= MAX_GOAL_TIPS) break;

            long pct = Math.round(g.progressPct());
            Map<String, Object> params = Map.of("name", g.name() != null ? g.name() : "", "pct", pct);

            if ("EXPENSE_LIMIT".equals(g.type())) {
                if (pct >= 90)      out.add(new TipCandidate("GOAL_EXPENSE_90", FinnyTipCategory.GOAL, "warning", 88, params));
                else if (pct >= 70) out.add(new TipCandidate("GOAL_EXPENSE_70", FinnyTipCategory.GOAL, "info",    68, params));
            } else {
                if (pct >= 75)      out.add(new TipCandidate("GOAL_SAVINGS_75", FinnyTipCategory.GOAL, "success", 72, params));
                else if (pct >= 50) out.add(new TipCandidate("GOAL_SAVINGS_50", FinnyTipCategory.GOAL, "info",    58, params));
            }

            if (out.size() < MAX_GOAL_TIPS && g.endDate() != null) {
                long daysLeft = ChronoUnit.DAYS.between(today, g.endDate());
                if (daysLeft >= 0 && daysLeft <= 7) {
                    out.add(new TipCandidate("GOAL_DEADLINE", FinnyTipCategory.GOAL, "info", 66,
                            Map.of("name", g.name() != null ? g.name() : "", "days", daysLeft)));
                }
            }
        }
        return out;
    }
}
