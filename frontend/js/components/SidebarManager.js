import { Icons } from '../icons/IconLibrary.js'
import { ThemeManager } from '../ThemeManager.js'
import { CustomSelect } from './CustomSelect.js'
import { I18n } from '../i18n.js'
import { LanguageSwitcher } from './LanguageSwitcher.js'
import { InputMasks } from '../utils/InputMasks.js'
import { NumberSpinner } from '../utils/NumberSpinner.js'
import { rerenderBreadcrumb } from '../../utils/FrontendFunctions.js'

const FLATPICKR_LOCALES = { pt: 'pt', es: 'es' }

export class SidebarManager {
    static _initialized = false

    static async initialize() {
        if (SidebarManager._initialized) return
        SidebarManager._initialized = true
        await I18n.initialize()
        SidebarManager.checkAuth()
        SidebarManager.renderIcons()
        SidebarManager.renderDataIcons()
        SidebarManager.setupActiveLink()
        SidebarManager.setupToggleButton()
        SidebarManager.setupOverlayDismiss()
        SidebarManager.initTranslations()
        ThemeManager.initialize()
        CustomSelect.autoInit()
        InputMasks.autoInit()
        NumberSpinner.autoInit()
        SidebarManager.initDatePickers()
        LanguageSwitcher.initialize()
        I18n.onChange(() => { SidebarManager.initTranslations(); InputMasks.reformatAll(); rerenderBreadcrumb() })
    }

    static onNavigate() {
        document.body.classList.remove('review-mode')
        SidebarManager.setupActiveLink()
        SidebarManager.renderDataIcons()
        InputMasks.autoInit()
        NumberSpinner.autoInit()
        SidebarManager.initDatePickers()
        SidebarManager.initTranslations()
    }

    static initTranslations() {
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key  = el.dataset.i18n
            const text = I18n.t(key)
            if (el.tagName === 'A') {
                const icon = el.querySelector('svg')
                el.innerHTML = icon ? icon.outerHTML : ''
                el.appendChild(document.createTextNode(text))
            } else if (el.tagName === 'OPTION') {
                el.textContent = text
            } else if (el.tagName === 'SPAN' && el.parentElement?.classList.contains('radio-option')) {
                el.textContent = text
            } else if (el.querySelector('[data-i18n]')) {
                // Has nested i18n children — update only the first text node to avoid destroying them
                for (const node of el.childNodes) {
                    if (node.nodeType === Node.TEXT_NODE) {
                        node.textContent = text + ' '
                        break
                    }
                }
            } else {
                el.textContent = text
            }
        })

        document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
            el.placeholder = I18n.t(el.dataset.i18nPlaceholder)
        })

        document.querySelectorAll('[data-i18n-title]').forEach(el => {
            const text = I18n.t(el.dataset.i18nTitle)
            if (el.classList.contains('info-hint-btn')) {
                el.dataset.tooltip = text
            } else {
                el.title = text
            }
        })

        document.querySelectorAll('[data-i18n-aria]').forEach(el => {
            el.setAttribute('aria-label', I18n.t(el.dataset.i18nAria))
        })
    }

    static initDatePickers() {
        if (typeof flatpickr === 'undefined') return
        const lang   = I18n.getLanguage()
        const locale = FLATPICKR_LOCALES[lang]
            ? (flatpickr.l10ns?.[FLATPICKR_LOCALES[lang]] ?? undefined)
            : undefined

        document.querySelectorAll('input[type="date"]:not([data-fp-init])').forEach(input => {
            input.dataset.fpInit = '1'
            const fp = flatpickr(input, {
                dateFormat:     'Y-m-d',
                altInput:       true,
                altFormat:      'd/m/Y',
                altInputClass:  'flatpickr-input fc-date-input',
                maxDate:        'today',
                defaultDate:    input.value || 'today',
                ...(locale ? { locale } : {}),
                disableMobile:  true,
                allowInput:     false,
                onChange: (_, __, instance) => {
                    instance.element.dispatchEvent(new Event('change', { bubbles: true }))
                }
            })
            fp.calendarContainer.addEventListener('wheel', e => {
                if (document.activeElement && document.activeElement.tagName === 'SELECT') return
                e.preventDefault()
                fp.changeMonth(e.deltaY > 0 ? 1 : -1)
            }, { passive: false })
            const orig = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')
            let syncing = false
            Object.defineProperty(input, 'value', {
                get()  { return orig.get.call(input) },
                set(v) {
                    if (syncing) { orig.set.call(input, v); return }
                    syncing = true
                    fp.setDate(v || null, false)
                    syncing = false
                },
                configurable: true
            })
        })
    }

    static checkAuth() {
        const pendingUrl = sessionStorage.getItem('__spa_url') ?? ''
        if (pendingUrl.includes('?guest=true')) return

        $.ajax({
            url: '/api/auth/me',
            type: 'GET',
            async: false,
            error: function (xhr) {
                if (xhr.status === 401) globalThis.location.href = '/pages/Login.html'
            }
        })
    }

    static renderIcons() {
        const iconMap = {
            'HomePage.html': 'home',
            'Dashboard.html': 'dashboard',
            'Transaction.html': 'transaction',
            'Transfer.html': 'transfer',
            'AccountDashboard.html': 'accounts',
            'CategoryDashboard.html': 'categories',
            'FinancialInstitutionDashboard.html': 'institutions',
            'TransactionLocaleDashboard.html': 'locations',
            'StatementImport.html': 'statementImport',
            'UserView.html': 'profile'
        }

        document.querySelectorAll('.sidebar-nav .sidebar-link, .sidebar-footer .sidebar-link').forEach(link => {
            const href     = link.getAttribute('href')?.split('/').pop()
            const iconName = href ? iconMap[href] : null
            if (!iconName) return

            const label = link.textContent.trim()
            link.innerHTML = `${Icons[iconName]()} ${label}`
        })

        const toggleButton = document.getElementById('sidebar-toggle-btn')
        if (toggleButton) {
            toggleButton.innerHTML = Icons.menu()
        }
    }

    static renderDataIcons() {
        document.querySelectorAll('[data-icon]').forEach(el => {
            const iconName = el.dataset.icon
            const icon = Icons[iconName]
            if (icon) el.innerHTML = icon()
        })
    }

    static setupActiveLink() {
        const links       = document.querySelectorAll('.sidebar-link[href]')
        const currentPage = location.pathname.split('/').pop()

        links.forEach(link => {
            const href = link.href.split('/').pop()
            link.classList.toggle('active', href === currentPage)
        })
    }

    static setupToggleButton() {
        const toggleButton = document.getElementById('sidebar-toggle-btn')
        const sidebar      = document.getElementById('sidebar')
        const overlay      = document.getElementById('sidebar-overlay')

        if (!toggleButton || !sidebar) return

        toggleButton.addEventListener('click', () => {
            sidebar.classList.toggle('open')
            if (overlay) overlay.classList.toggle('show')
        })
    }

    static setupOverlayDismiss() {
        const overlay = document.getElementById('sidebar-overlay')
        const sidebar = document.getElementById('sidebar')

        if (!overlay) return

        overlay.addEventListener('click', () => {
            sidebar?.classList.remove('open')
            overlay.classList.remove('show')
        })
    }
}
