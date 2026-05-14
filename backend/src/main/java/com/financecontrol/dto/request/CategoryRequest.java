package com.financecontrol.dto.request;

import java.util.List;

public record CategoryRequest(
    String name,
    String description,
    List<String> aliases
) {}
