package com.financecontrol.dto.response;

import com.financecontrol.entity.Goal;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record GoalResponse(
        Long id,
        String name,
        String description,
        GoalType type,
        GoalStatus status,
        Double targetAmount,
        LocalDate startDate,
        LocalDate endDate,
        List<CategoryResponse> categories,
        List<TransactionLocaleResponse> locales,
        Boolean notifyAt50,
        Boolean notifyAt75,
        Boolean notifyAt90,
        Boolean notifyOnComplete,
        Boolean notifyOnDeadline,
        Boolean notifyOnExceed,
        Double currentAmount,
        Double progressPercent,
        LocalDateTime createdAt
) {
    public static GoalResponse from(Goal g, double currentAmount) {
        double pct = g.getTargetAmount() != null && g.getTargetAmount() > 0
                ? (currentAmount / g.getTargetAmount()) * 100.0 : 0.0;
        return new GoalResponse(
                g.getId(),
                g.getName(),
                g.getDescription(),
                g.getType(),
                g.getStatus(),
                g.getTargetAmount(),
                g.getStartDate(),
                g.getEndDate(),
                g.getCategories().stream().map(CategoryResponse::from).toList(),
                g.getLocales().stream().map(TransactionLocaleResponse::from).toList(),
                g.getNotifyAt50(),
                g.getNotifyAt75(),
                g.getNotifyAt90(),
                g.getNotifyOnComplete(),
                g.getNotifyOnDeadline(),
                g.getNotifyOnExceed(),
                currentAmount,
                Math.round(pct * 100.0) / 100.0,
                g.getCreatedAt()
        );
    }
}
