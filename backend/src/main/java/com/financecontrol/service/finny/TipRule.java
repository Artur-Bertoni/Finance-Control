package com.financecontrol.service.finny;

import java.util.List;

/**
 * Uma regra de aconselhamento ("advisor"). Lê o perfil financeiro e devolve 0+ dicas candidatas.
 * Implementações são {@code @Component} e injetadas como {@code List<TipRule>} no agente —
 * adicionar uma nova capacidade ao Finny = criar uma nova classe que implemente esta interface.
 */
public interface TipRule {
    List<TipCandidate> evaluate(FinancialProfile profile);
}
