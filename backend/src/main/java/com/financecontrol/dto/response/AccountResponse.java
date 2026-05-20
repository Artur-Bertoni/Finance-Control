package com.financecontrol.dto.response;

import com.financecontrol.entity.Account;
import java.time.LocalDateTime;

public record AccountResponse(
    Long id,
    FinancialInstitutionResponse financialInstitution,
    String name,
    String contact,
    String description,
    Double balance,
    String iconKey,
    LocalDateTime createdAt
) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(
                a.getId(),
                FinancialInstitutionResponse.from(a.getFinancialInstitution()),
                a.getName(), a.getContact(), a.getDescription(), a.getBalance(),
                a.getIconKey(), a.getCreatedAt());
    }
}
