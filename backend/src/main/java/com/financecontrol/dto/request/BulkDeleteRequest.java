package com.financecontrol.dto.request;

import com.financecontrol.enums.BulkEntityType;

import java.util.List;

public record BulkDeleteRequest(BulkEntityType type,
                                List<Long> ids) {
}
