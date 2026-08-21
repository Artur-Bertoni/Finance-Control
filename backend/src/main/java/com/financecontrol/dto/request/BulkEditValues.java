package com.financecontrol.dto.request;

import com.financecontrol.enums.AccountType;
import com.financecontrol.enums.GoalType;
import com.financecontrol.enums.TransactionType;

import java.time.LocalDate;

public record BulkEditValues(
        Long accountId,
        Long categoryId,
        Long transactionLocaleId,
        Long financialInstitutionId,
        Double value,
        LocalDate date,
        TransactionType transactionType,
        String obs,
        String description,
        String address,
        String contact,
        String iconKey,
        AccountType accountType,
        GoalType goalType,
        Double targetAmount,
        LocalDate startDate,
        LocalDate endDate,
        Boolean notifyAt50,
        Boolean notifyAt75,
        Boolean notifyAt90,
        Boolean notifyOnComplete,
        Boolean notifyOnDeadline,
        Boolean notifyOnExceed,
        Double monthlyLimit
) {}
