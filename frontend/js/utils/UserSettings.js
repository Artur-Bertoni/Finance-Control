const DEFAULTS = {
    reportsEnabled:         true,
    budgetsEnabled:         true,
    goalsEnabled:           true,
    finnyEnabled:           true,
    statementImportEnabled: true,
    institutionsEnabled:    true,
    localesEnabled:         true,
    emailsEnabled:          true,
}

const FEATURE_BY_KEY = {
    reportsEnabled:         'reports',
    budgetsEnabled:         'budgets',
    goalsEnabled:           'goals',
    finnyEnabled:           'finny',
    statementImportEnabled: 'statementImport',
    institutionsEnabled:    'institutions',
    localesEnabled:         'locales',
    emailsEnabled:          'emails',
}

export const SETTING_KEYS = Object.keys(DEFAULTS)

export class UserSettings {
    static all() {
        return { ...DEFAULTS, ...(globalThis.__currentUser?.settings ?? {}) }
    }

    static isEnabled(key) {
        return UserSettings.all()[key] !== false
    }

    static get reports()         { return UserSettings.isEnabled('reportsEnabled') }
    static get budgets()         { return UserSettings.isEnabled('budgetsEnabled') }
    static get goals()           { return UserSettings.isEnabled('goalsEnabled') }
    static get finny()           { return UserSettings.isEnabled('finnyEnabled') }
    static get statementImport() { return UserSettings.isEnabled('statementImportEnabled') }
    static get institutions()    { return UserSettings.isEnabled('institutionsEnabled') }
    static get locales()         { return UserSettings.isEnabled('localesEnabled') }
    static get emails()          { return UserSettings.isEnabled('emailsEnabled') }

    static store(settings) {
        if (globalThis.__currentUser) globalThis.__currentUser.settings = settings
        UserSettings.applyFlags()
    }

    static applyFlags() {
        const settings = UserSettings.all()
        const root     = document.documentElement
        for (const [key, feature] of Object.entries(FEATURE_BY_KEY)) {
            root.classList.toggle(`feature-${feature}-off`, settings[key] === false)
        }
    }
}
