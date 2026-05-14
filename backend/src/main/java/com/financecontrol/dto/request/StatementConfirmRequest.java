package com.financecontrol.dto.request;

import java.util.List;

public record StatementConfirmRequest(
    Long accountId,
    List<ImportRowRequest> rows
) {}
