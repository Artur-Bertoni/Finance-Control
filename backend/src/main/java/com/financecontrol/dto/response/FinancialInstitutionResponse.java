package com.financecontrol.dto.response;

import com.financecontrol.entity.FinancialInstitution;
import java.time.LocalDateTime;

public record FinancialInstitutionResponse(
    Long id,
    String name,
    String address,
    String contact,
    String iconKey,
    LocalDateTime createdAt
) {
    public static FinancialInstitutionResponse from(FinancialInstitution fi) {
        return new FinancialInstitutionResponse(fi.getId(), fi.getName(), fi.getAddress(), fi.getContact(), fi.getIconKey(), fi.getCreatedAt());
    }
}
