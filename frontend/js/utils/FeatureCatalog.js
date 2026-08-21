export const FEATURE_ROWS = [
    { key: 'reportsEnabled',         titleKey: 'featureReports',         descKey: 'featureReportsDesc',         offKey: 'featureReportsOff' },
    { key: 'budgetsEnabled',         titleKey: 'featureBudgets',         descKey: 'featureBudgetsDesc',         offKey: 'featureBudgetsOff' },
    { key: 'goalsEnabled',           titleKey: 'featureGoals',           descKey: 'featureGoalsDesc',           offKey: 'featureGoalsOff' },
    { key: 'finnyEnabled',           titleKey: 'featureFinny',           descKey: 'featureFinnyDesc',           offKey: 'featureFinnyOff' },
    { key: 'statementImportEnabled', titleKey: 'featureStatementImport', descKey: 'featureStatementImportDesc', offKey: 'featureStatementImportOff' },
    { key: 'institutionsEnabled',    titleKey: 'featureInstitutions',    descKey: 'featureInstitutionsDesc',    offKey: 'featureInstitutionsOff' },
    { key: 'localesEnabled',         titleKey: 'featureLocales',         descKey: 'featureLocalesDesc',         offKey: 'featureLocalesOff' },
    { key: 'emailsEnabled',          titleKey: 'featureEmails',          descKey: 'featureEmailsDesc',          offKey: 'featureEmailsOff' },
]

export const PROFILE_PRESETS = {
    simple: {
        reportsEnabled:         false,
        budgetsEnabled:         false,
        goalsEnabled:           false,
        finnyEnabled:           false,
        statementImportEnabled: false,
        institutionsEnabled:    false,
        localesEnabled:         false,
        emailsEnabled:          true,
    },
    complete: {
        reportsEnabled:         true,
        budgetsEnabled:         true,
        goalsEnabled:           true,
        finnyEnabled:           true,
        statementImportEnabled: true,
        institutionsEnabled:    true,
        localesEnabled:         true,
        emailsEnabled:          true,
    },
}
