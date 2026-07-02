package com.financecontrol.dto.request;

import org.jspecify.annotations.NonNull;
import java.time.LocalDate;

public record PayInvoiceRequest(
    @NonNull Long sourceAccountId,
    Long categoryId,
    LocalDate date
) {}
