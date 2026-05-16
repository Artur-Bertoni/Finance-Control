package com.financecontrol.dto.response;

import java.util.List;

public record TransactionCreateResponse(
        TransactionResponse transaction,
        List<AppNotificationResponse> notifications
) {}
