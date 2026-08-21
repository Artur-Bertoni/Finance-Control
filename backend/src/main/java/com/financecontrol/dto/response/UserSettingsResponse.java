package com.financecontrol.dto.response;

import com.financecontrol.entity.UserSettings;

public record UserSettingsResponse(
    boolean reportsEnabled,
    boolean budgetsEnabled,
    boolean goalsEnabled,
    boolean finnyEnabled,
    boolean statementImportEnabled,
    boolean institutionsEnabled,
    boolean localesEnabled,
    boolean emailsEnabled
) {
    public static UserSettingsResponse defaults() {
        return new UserSettingsResponse(true, true, true, true, true, true, true, true);
    }

    public static UserSettingsResponse from(UserSettings s) {
        if (s == null) return defaults();
        return new UserSettingsResponse(s.isReportsEnabled(), s.isBudgetsEnabled(),
                s.isGoalsEnabled(), s.isFinnyEnabled(), s.isStatementImportEnabled(),
                s.isInstitutionsEnabled(), s.isLocalesEnabled(), s.isEmailsEnabled());
    }
}
