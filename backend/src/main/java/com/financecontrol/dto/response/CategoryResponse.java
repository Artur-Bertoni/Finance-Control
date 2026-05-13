package com.financecontrol.dto.response;

import com.financecontrol.entity.Category;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    String internalName
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.getInternalName());
    }
}
