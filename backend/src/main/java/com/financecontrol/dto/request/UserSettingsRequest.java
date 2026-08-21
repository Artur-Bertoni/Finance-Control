package com.financecontrol.dto.request;

public record UserSettingsRequest(
    Boolean reportsEnabled,
    Boolean budgetsEnabled,
    Boolean goalsEnabled,
    Boolean finnyEnabled,
    Boolean statementImportEnabled,
    Boolean institutionsEnabled,
    Boolean localesEnabled,
    Boolean emailsEnabled
) {}
