package com.financecontrol.dto.response;

import com.financecontrol.entity.User;

public record UserResponse(
    Long id,
    String username,
    String email,
    boolean emailNotificationEnabled,
    int emailNotificationDay,
    String language,
    boolean admin
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(),
                u.isEmailNotificationEnabled(), u.getEmailNotificationDay(),
                u.getLanguage(), u.isAdmin());
    }
}
