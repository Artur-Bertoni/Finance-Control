package com.financecontrol.dto.response;

import com.financecontrol.entity.User;

public record UserResponse(Long id, String username, String email) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail());
    }
}
