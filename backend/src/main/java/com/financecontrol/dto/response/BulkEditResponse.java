package com.financecontrol.dto.response;

public record BulkEditResponse(int requested,
                               int edited,
                               int skipped) {
}
