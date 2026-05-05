package com.financecontrol.dto.request;

public record LoginRequest(
    String email,
    String password
) {}
