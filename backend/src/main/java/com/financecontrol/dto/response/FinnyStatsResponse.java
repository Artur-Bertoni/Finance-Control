package com.financecontrol.dto.response;

import java.util.Map;

/**
 * Resumo para o painel de progresso do Finny: quantas dicas, como o usuário reagiu,
 * distribuição por categoria e um retrato atual de saúde financeira.
 */
public record FinnyStatsResponse(
        long totalTips,
        long helpfulCount,
        long notHelpfulCount,
        long dismissedCount,
        Map<String, Long> byCategory,
        double savingsRatePct,
        double emergencyFundMonths,
        double net,
        double currentBalance
) {}
