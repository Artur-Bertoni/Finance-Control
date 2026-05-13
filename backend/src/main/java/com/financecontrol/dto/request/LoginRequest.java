package com.financecontrol.dto.request;

public record LoginRequest(
    String identifier,
    String password
) {}
