package com.financecontrol.dto.response;

import com.financecontrol.entity.User;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String username,
    String email,
    boolean emailNotificationEnabled,
    int emailNotificationDay,
    boolean goalEmailNotificationEnabled,
    String language,
    boolean admin,
    LocalDateTime createdAt,
    boolean googleLinked,
    boolean hasPassword
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(),
                u.isEmailNotificationEnabled(), u.getEmailNotificationDay(),
                u.isGoalEmailNotificationEnabled(),
                u.getLanguage(), u.isAdmin(), u.getCreatedAt(),
                "google".equals(u.getProvider()),
                u.getPassword() != null && !u.getPassword().isBlank());
    }
}
