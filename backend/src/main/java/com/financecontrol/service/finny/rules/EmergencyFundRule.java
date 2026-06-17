package com.financecontrol.service.finny.rules;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.service.finny.FinancialProfile;
import com.financecontrol.service.finny.TipCandidate;
import com.financecontrol.service.finny.TipRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Reserva de emergência (saldo ÷ despesa média mensal):
 *  - menos de 3 meses  → incentiva construir a reserva (SAVINGS);
 *  - 6 meses ou mais e com sobra → dica EDUCACIONAL de investimento do excedente (INVESTMENT).
 * Entre 3 e 6 meses não emite nada (zona saudável).
 */
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
