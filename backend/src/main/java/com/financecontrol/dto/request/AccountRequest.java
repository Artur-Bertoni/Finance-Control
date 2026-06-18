package com.financecontrol.dto.request;

import com.financecontrol.enums.AccountType;
import org.springframework.lang.NonNull;

public record AccountRequest(
    @NonNull Long financialInstitutionId,
    String name,
    String contact,
    String description,
    Double balance,
    String iconKey,
    AccountType type,
    Integer closingDay,
    Integer dueDay
) {
    public AccountRequest(@NonNull Long financialInstitutionId, String name, String contact,
                          String description, Double balance, String iconKey) {
        this(financialInstitutionId, name, contact, description, balance, iconKey, null, null, null);
    }
}
