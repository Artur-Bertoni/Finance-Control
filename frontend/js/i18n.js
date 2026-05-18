export class I18n {
    static _currentLanguage = localStorage.getItem('language') || 'pt'
    static _translations = {}
    static _listeners = []

    static async initialize() {
        await Promise.all([
            this.loadLanguage('pt'),
            this.loadLanguage('en'),
            this.loadLanguage('es')
        ])
        this.setLanguage(this._currentLanguage)
    }

    static async loadLanguage(lang) {
        try {
            const response = await fetch(`/locales/${lang}.json`)
            this._translations[lang] = await response.json()
        } catch {
            // language file missing — key fallback handles missing translations
        }
    }

    static t(key, params = {}) {
        let text = this._translations[this._currentLanguage]?.[key] || key

        if (Object.keys(params).length > 0) {
            Object.entries(params).forEach(([k, v]) => {
                text = text.replaceAll(new RegExp(`{${k}}`, 'g'), v)
            })
        }

        return text
    }

    static setLanguage(lang) {
        if (!this._translations[lang]) return
        this._currentLanguage = lang
        localStorage.setItem('language', lang)
        document.documentElement.lang = lang
        this._notifyListeners()
    }

    static getLanguage() {
        return this._currentLanguage
    }

    static onChange(callback) {
        this._listeners.push(callback)
    }

    static _notifyListeners() {
        this._listeners.forEach(cb => cb(this._currentLanguage))
    }

    static getAvailableLanguages() {
        return ['pt', 'en', 'es']
    }
}
