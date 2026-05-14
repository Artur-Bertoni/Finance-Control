package com.financecontrol.dto.request;

import com.financecontrol.enums.GoalType;
import java.time.LocalDate;
import java.util.List;

public record FinancialGoalRequest(
        String name,
        String description,
        GoalType type,
        Double targetAmount,
        LocalDate startDate,
        LocalDate endDate,
        List<Long> categoryIds,
        List<Long> localeIds,
        Boolean notifyAt50,
        Boolean notifyAt75,
        Boolean notifyAt90,
        Boolean notifyOnComplete,
        Boolean notifyOnDeadline,
        Boolean notifyOnExceed
) {}
