package com.financecontrol.dto.request;

public record TransactionLocaleRequest(
    String name,
    String address,
    String iconKey
) {}
