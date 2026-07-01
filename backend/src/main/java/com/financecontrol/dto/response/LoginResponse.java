package com.financecontrol.dto.response;

public record LoginResponse(
    String token,
    UserResponse user
) {}
