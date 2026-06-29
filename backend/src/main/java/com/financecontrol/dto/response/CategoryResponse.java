package com.financecontrol.dto.response;

import com.financecontrol.entity.Category;
import com.financecontrol.entity.CategoryAlias;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("null")
public record CategoryResponse(
    Long id,
    String name,
    String description,
    String iconKey,
    List<String> aliases,
    LocalDateTime createdAt
) {
    public static CategoryResponse from(Category c) {
        List<String> aliasesList = c.getAliases().stream()
                .map(CategoryAlias::getAliasName)
                .toList();
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.getIconKey(), aliasesList, c.getCreatedAt());
    }
}
