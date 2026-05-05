package com.financecontrol.dto.request;

import java.time.LocalDate;

public record TransactionRequest(
        Long accountId,
        Long categoryId,
        Long transactionLocaleId,
        Double value,
        LocalDate date,
        String type,
        Integer installmentsNumber,
        String obs,
        Long transferPartnerId) {}
