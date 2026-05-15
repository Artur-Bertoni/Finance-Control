package com.financecontrol.dto.response;

import java.time.LocalDateTime;

public record AchievementResponse(
        String type,
        String tier,
        String iconKey,
        boolean earned,
        LocalDateTime earnedAt
) {}
