package com.financecontrol.dto.request;

import com.financecontrol.enums.TransactionType;
import java.time.LocalDate;

public record TransactionRequest(
        Long accountId,
        Long categoryId,
        Long transactionLocaleId,
        Double value,
        LocalDate date,
        TransactionType type,
        Integer installmentsNumber,
        String obs,
        Long transferPartnerId,
        String invoiceReference
) {
    public TransactionRequest(Long accountId, Long categoryId, Long transactionLocaleId, Double value,
                              LocalDate date, TransactionType type, Integer installmentsNumber,
                              String obs, Long transferPartnerId) {
        this(accountId, categoryId, transactionLocaleId, value, date, type, installmentsNumber, obs, transferPartnerId, null);
    }
}
