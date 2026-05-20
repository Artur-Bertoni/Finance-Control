package com.financecontrol.dto.request;

import java.time.LocalDate;
import org.springframework.lang.NonNull;

public record TransferRequest(
        @NonNull Long originAccountId,
        @NonNull Long destinationAccountId,
        Long categoryId,
        Long transactionLocaleId,
        Double value,
        LocalDate date,
        String obs
) {}
