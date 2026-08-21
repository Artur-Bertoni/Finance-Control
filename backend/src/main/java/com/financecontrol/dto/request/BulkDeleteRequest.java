package com.financecontrol.dto.request;

import com.financecontrol.enums.BulkDeleteType;

import java.util.List;

public record BulkDeleteRequest(BulkDeleteType type,
                                List<Long> ids) {
}
