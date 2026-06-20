package com.financecontrol.service.finny;

import java.util.List;

public interface TipRule {
    List<TipCandidate> evaluate(FinancialProfile profile);
}
