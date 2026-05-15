package com.financecontrol.dto.response;

import com.financecontrol.entity.FinancialInstitution;

public record FinancialInstitutionResponse(
    Long id,
    String name,
    String address,
    String contact,
    String iconKey
) {
    public static FinancialInstitutionResponse from(FinancialInstitution fi) {
        return new FinancialInstitutionResponse(fi.getId(), fi.getName(), fi.getAddress(), fi.getContact(), fi.getIconKey());
    }
}
