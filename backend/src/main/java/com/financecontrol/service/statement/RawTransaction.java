package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;

import java.time.LocalDate;

public record RawTransaction(
    LocalDate date,
    String description,
    double amount,
    TransactionType type,
    String installmentLabel,
    String invoiceReference
) {
    public RawTransaction(LocalDate date, String description, double amount, TransactionType type) {
        this(date, description, amount, type, null, null);
    }
}
