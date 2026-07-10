package com.financecontrol.dto.request;

import org.jspecify.annotations.NonNull;

public record ReconcileInvoiceRequest(
    @NonNull Long paymentTransactionId
) {}
