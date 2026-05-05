package com.financecontrol.dto.request;

import java.time.LocalDate;

public record TransferRequest(
        Long originAccountId,
        Long destinationAccountId,
        Long categoryId,
        Long transactionLocaleId,
        Double value,
        LocalDate date,
        String obs) {}
