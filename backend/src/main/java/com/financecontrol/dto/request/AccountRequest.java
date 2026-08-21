package com.financecontrol.dto.request;

import com.financecontrol.enums.AccountType;

public record AccountRequest(
    Long financialInstitutionId,
    String name,
    String contact,
    String description,
    Double balance,
    String iconKey,
    AccountType type,
    Integer closingDay,
    Integer dueDay,
    Double creditLimit
) {
    public AccountRequest(Long financialInstitutionId, String name, String contact,
                          String description, Double balance, String iconKey) {
        this(financialInstitutionId, name, contact, description, balance, iconKey, null, null, null, null);
    }
}
