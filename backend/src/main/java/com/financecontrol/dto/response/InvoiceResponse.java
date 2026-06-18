package com.financecontrol.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceResponse(
    String referenceMonth,
    LocalDate closingDate,
    LocalDate dueDate,
    Double total,
    int itemCount,
    String status,
    LocalDateTime paidAt,
    Long paymentTransactionId
) {}
