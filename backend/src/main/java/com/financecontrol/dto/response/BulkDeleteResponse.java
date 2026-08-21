package com.financecontrol.dto.response;

public record BulkDeleteResponse(int requested,
                                 int deleted,
                                 int skipped) {
}
