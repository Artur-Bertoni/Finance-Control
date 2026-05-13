package com.financecontrol.dto.request;

public record CategoryRequest(
    String name,
    String description,
    String internalName
) {}
