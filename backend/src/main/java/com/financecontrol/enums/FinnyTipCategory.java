package com.financecontrol.enums;

/**
 * Categoria temática de uma dica do Finny.
 * É a "dimensão" usada para aprender as preferências do usuário: o peso adaptativo
 * (FinnyTipPreference.weight) é guardado por categoria, não por dica individual.
 */
public enum FinnyTipCategory {
    BUDGET,      // orçamento / onde o dinheiro está indo
    SAVINGS,     // poupança / reserva de emergência
    INVESTMENT,  // investimentos (educacional, não-prescritivo)
    GOAL,        // progresso de metas financeiras
    CASHFLOW,    // fluxo de caixa / evolução de patrimônio
    ENGAGEMENT   // incentivo a usar o app (ex: registrar transações)
}
