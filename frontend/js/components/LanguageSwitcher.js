import { I18n } from '../i18n.js'

export class LanguageSwitcher {
    static _container = null
    static _initialized = false

    static initialize() {
        if (LanguageSwitcher._initialized) return
        LanguageSwitcher._initialized = true
        LanguageSwitcher.render()
        I18n.onChange(() => LanguageSwitcher.updateActive())
    }

    static render() {
        const container = document.querySelector('.sidebar-footer')
        if (!container) return

        const wrapper = document.createElement('div')
        wrapper.className = 'language-switcher'
        wrapper.innerHTML = `
            <button class="language-trigger" title="${I18n.t('selectLanguage')}" aria-label="${I18n.t('selectLanguage')}">
                <span class="fi fi-squared flag-icon" id="current-flag"></span>
            </button>
            <div class="language-menu" id="language-menu">
                <button class="language-option" data-lang="pt" title="Português" aria-label="Português">
                    <span class="fi fi-squared fi-br flag-icon"></span>
                    <span class="language-option-label">Português</span>
                </button>
                <button class="language-option" data-lang="en" title="English" aria-label="English">
                    <span class="fi fi-squared fi-us flag-icon"></span>
                    <span class="language-option-label">English</span>
                </button>
                <button class="language-option" data-lang="es" title="Español" aria-label="Español">
                    <span class="fi fi-squared fi-es flag-icon"></span>
                    <span class="language-option-label">Español</span>
                </button>
            </div>
        `
        container.appendChild(wrapper)
        LanguageSwitcher._container = wrapper
        LanguageSwitcher.attachEvents()
        LanguageSwitcher.updateActive()
    }

    static attachEvents() {
        const trigger = document.querySelector('.language-trigger')
        const menu = document.getElementById('language-menu')
        const options = document.querySelectorAll('.language-option')

        if (!trigger || !menu) return

        trigger.addEventListener('click', (e) => {
            e.stopPropagation()
            menu.classList.toggle('show')
        })

        options.forEach(option => {
            option.addEventListener('click', () => {
                const lang = option.dataset.lang
                I18n.setLanguage(lang)
                menu.classList.remove('show')
            })
        })

        document.addEventListener('click', () => {
            menu.classList.remove('show')
        })
    }

    static updateActive() {
        const currentLang = I18n.getLanguage()
        const flagCodes   = { pt: 'fi-br', en: 'fi-us', es: 'fi-es' }

        const currentFlag = document.getElementById('current-flag')
        if (currentFlag) {
            currentFlag.className = `fi fi-squared ${flagCodes[currentLang] ?? 'fi-br'} flag-icon`
        }
        document.querySelectorAll('.language-option').forEach(option => {
            option.classList.toggle('active', option.dataset.lang === currentLang)
        })
    }
}
