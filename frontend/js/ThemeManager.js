import { Icons } from './icons/IconLibrary.js'
import { I18n } from './i18n.js'

export class ThemeManager {
    static initialize() {
        ThemeManager.applyTheme()
        ThemeManager.injectToggle()
        ThemeManager.watchGuestMode()
    }

    static applyTheme() {
        const saved = localStorage.getItem('theme') || 'light'
        document.documentElement.dataset.theme = saved
    }

    static toggle() {
        const current = document.documentElement.dataset.theme || 'light'
        const next = current === 'dark' ? 'light' : 'dark'

        document.documentElement.classList.add('theme-transitioning')
        document.documentElement.dataset.theme = next
        localStorage.setItem('theme', next)
        ThemeManager.updateToggleIcon()

        setTimeout(() => document.documentElement.classList.remove('theme-transitioning'), 300)
    }

    static injectToggle() {
        const header = document.querySelector('.sidebar-header')
        if (!header) {
            ThemeManager.injectFloatingToggle()
            return
        }
        if (header.querySelector('.theme-toggle')) return

        const btn = document.createElement('button')
        btn.className = 'theme-toggle'
        btn.type = 'button'
        btn.setAttribute('aria-label', 'Alternar tema')
        btn.addEventListener('click', ThemeManager.toggle)
        header.appendChild(btn)

        ThemeManager.updateToggleIcon()
    }

    static injectFloatingToggle() {
        if (document.querySelector('.theme-toggle-floating')) return

        const btn = document.createElement('button')
        btn.className = 'theme-toggle-floating'
        btn.type = 'button'
        btn.setAttribute('aria-label', 'Alternar tema')
        btn.addEventListener('click', ThemeManager.toggle)
        document.body.appendChild(btn)

        ThemeManager.updateToggleIcon()
    }

    static watchGuestMode() {
        if (!document.querySelector('.sidebar-header')) return

        const observer = new MutationObserver(() => {
            if (document.body.classList.contains('user-guest')) {
                ThemeManager.injectFloatingToggle()
                observer.disconnect()
            }
        })

        observer.observe(document.body, { attributes: true, attributeFilter: ['class'] })
    }

    static updateToggleIcon() {
        const isDark = document.documentElement.dataset.theme === 'dark'
        const icon  = isDark ? Icons.sun() : Icons.moon()
        const title = isDark ? I18n.t('switchToLightMode') : I18n.t('switchToDarkMode')

        document.querySelectorAll('.theme-toggle, .theme-toggle-floating').forEach(btn => {
            btn.innerHTML = icon
            btn.title = title
        })
    }
}
