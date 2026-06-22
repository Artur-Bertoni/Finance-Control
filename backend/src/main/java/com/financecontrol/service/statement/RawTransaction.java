package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;

import java.time.LocalDate;

public record RawTransaction(
    LocalDate date,
    String description,
    double amount,
    TransactionType type
) {}
