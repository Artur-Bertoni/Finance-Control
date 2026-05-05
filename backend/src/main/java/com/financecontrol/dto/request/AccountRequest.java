package com.financecontrol.dto.request;

public record AccountRequest(Long financialInstitutionId, String name, String contact, String description, Double balance) {}
