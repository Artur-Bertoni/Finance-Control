package com.financecontrol.dto.response;

import com.financecontrol.entity.UserSettings;

import java.util.Arrays;
import java.util.List;

public record UserSettingsResponse(
    boolean reportsEnabled,
    boolean budgetsEnabled,
    boolean goalsEnabled,
    boolean finnyEnabled,
    boolean statementImportEnabled,
    boolean institutionsEnabled,
    boolean localesEnabled,
    boolean emailsEnabled,
    List<Long> chartExpensePinnedCategories,
    List<Long> chartExpenseGroupedCategories,
    List<Long> chartIncomePinnedCategories,
    List<Long> chartIncomeGroupedCategories
) {
    public static UserSettingsResponse defaults() {
        return new UserSettingsResponse(true, true, true, true, true, true, true, true,
                List.of(), List.of(), List.of(), List.of());
    }

    public static UserSettingsResponse from(UserSettings s) {
        if (s == null) return defaults();
        return new UserSettingsResponse(s.isReportsEnabled(), s.isBudgetsEnabled(),
                s.isGoalsEnabled(), s.isFinnyEnabled(), s.isStatementImportEnabled(),
                s.isInstitutionsEnabled(), s.isLocalesEnabled(), s.isEmailsEnabled(),
                parseIds(s.getChartExpensePinnedCategories()), parseIds(s.getChartExpenseGroupedCategories()),
                parseIds(s.getChartIncomePinnedCategories()), parseIds(s.getChartIncomeGroupedCategories()));
    }

    private static List<Long> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty() && part.chars().allMatch(Character::isDigit))
                .map(Long::valueOf)
                .toList();
    }
}
