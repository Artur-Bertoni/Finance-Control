package com.financecontrol.dto.response;

import com.financecontrol.entity.Transaction;
import com.financecontrol.enums.TransactionType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
    Long id,
    AccountResponse account,
    CategoryResponse category,
    TransactionLocaleResponse transactionLocale,
    Double value,
    LocalDate date,
    TransactionType type,
    Integer installmentsNumber,
    String obs,
    Long transferPartnerId,
    Long installmentGroupId,
    Integer installmentIndex,
    Boolean applied,
    Double installmentTotalValue,
    LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return from(t, null);
    }

    public static TransactionResponse from(Transaction t, Double installmentTotalValue) {
        return new TransactionResponse(
                t.getId(),
                AccountResponse.from(t.getAccount()),
                CategoryResponse.from(t.getCategory()),
                t.getTransactionLocale() != null ? TransactionLocaleResponse.from(t.getTransactionLocale()) : null,
                t.getValue(),
                t.getDate(),
                t.getType(),
                t.getInstallmentsNumber(),
                t.getObs(),
                t.getTransferPartnerId(),
                t.getInstallmentGroupId(),
                t.getInstallmentIndex(),
                t.getApplied(),
                installmentTotalValue,
                t.getCreatedAt());
    }
}
