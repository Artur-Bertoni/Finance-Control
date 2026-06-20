package com.financecontrol.dto.response;

import java.util.Map;

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
