package com.financecontrol.dto.response;

import com.financecontrol.entity.AppNotification;
import com.financecontrol.enums.AppNotificationType;
import java.time.LocalDateTime;

public record AppNotificationResponse(
        Long id,
        AppNotificationType type,
        Long goalId,
        String goalName,
        Long transactionId,
        String link,
        boolean read,
        LocalDateTime createdAt
) {
    public static AppNotificationResponse from(AppNotification n) {
        return new AppNotificationResponse(
                n.getId(),
                n.getType(),
                n.getGoalId(),
                n.getGoalName(),
                n.getTransactionId(),
                n.getLink(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
