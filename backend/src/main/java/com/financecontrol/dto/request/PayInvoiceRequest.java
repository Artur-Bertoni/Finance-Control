package com.financecontrol.dto.request;

import org.springframework.lang.NonNull;
import java.time.LocalDate;

public record PayInvoiceRequest(
    @NonNull Long sourceAccountId,
    Long categoryId,
    LocalDate date
) {}
