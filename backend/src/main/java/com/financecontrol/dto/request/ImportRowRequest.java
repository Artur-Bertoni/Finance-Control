package com.financecontrol.dto.request;

import com.financecontrol.enums.TransactionType;

public record ImportRowRequest(
    String date,
    String description,
    double amount,
    TransactionType type,
    Long categoryId,
    Long localeId,
    boolean skip,
    String installmentLabel,
    String invoiceReference
) {
    public ImportRowRequest(String date, String description, double amount, TransactionType type,
                            Long categoryId, Long localeId, boolean skip) {
        this(date, description, amount, type, categoryId, localeId, skip, null, null);
    }
}
