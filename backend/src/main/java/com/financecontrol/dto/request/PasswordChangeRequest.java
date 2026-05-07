package com.financecontrol.dto.request;

public record PasswordChangeRequest(
    String currentPassword,
    String newPassword,
    String passwordConfirmation
) {}
