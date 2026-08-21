package com.financecontrol.dto.request;

import com.financecontrol.enums.BulkEntityType;

import java.util.List;
import java.util.Set;

public record BulkEditRequest(BulkEntityType type,
                              List<Long> ids,
                              Set<String> fields,
                              BulkEditValues values) {
}
