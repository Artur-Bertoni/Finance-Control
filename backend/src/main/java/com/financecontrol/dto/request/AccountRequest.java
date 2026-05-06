package com.financecontrol.dto.request;

import org.springframework.lang.NonNull;

public record AccountRequest(
    @NonNull Long financialInstitutionId,
    String name,
    String contact,
    String description,
    Double balance
) {}
