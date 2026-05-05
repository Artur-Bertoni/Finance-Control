package com.financecontrol.dto.request;

public record UserRequest(String username, String email, String password, String passwordConfirmation) {}
