import { Icons } from '../icons/IconLibrary.js'
import { ThemeManager } from '../ThemeManager.js'
import { CustomSelect } from './CustomSelect.js'
import { I18n } from '../i18n.js'
import { LanguageSwitcher } from './LanguageSwitcher.js'
import { InputMasks } from '../utils/InputMasks.js'
import { NumberSpinner } from '../utils/NumberSpinner.js'
import { rerenderBreadcrumb, showToast, navigate } from '../../utils/FrontendFunctions.js'
import { SearchManager } from './SearchManager.js'

const FLATPICKR_LOCALES = { pt: 'pt', es: 'es' }

export class SidebarManager {
    static _initialized = false

    static async initialize() {
        if (SidebarManager._initialized) return
        SidebarManager._initialized = true
        await I18n.initialize()
        SidebarManager.checkAuth()
        SidebarManager.applyAdminVisibility()
        SidebarManager.renderIcons()
        SidebarManager.renderDataIcons()
        SidebarManager.setupActiveLink()
        SidebarManager.setupAdvancedNav()
        SidebarManager.setupToggleButton()
        SidebarManager.setupOverlayDismiss()
        SidebarManager.initTranslations()
        ThemeManager.initialize()
        CustomSelect.autoInit()
        InputMasks.autoInit()
        NumberSpinner.autoInit()
        SidebarManager.initDatePickers()
        LanguageSwitcher.initialize()
        SearchManager.initialize()
        I18n.onChange(() => { SidebarManager.initTranslations(); InputMasks.reformatAll(); rerenderBreadcrumb(); SidebarManager.updateDatePickerLocales() })
        SidebarManager.checkAchievements()
        SidebarManager.checkGoalCompletions()
        SidebarManager.refreshNotificationBadge()
        SidebarManager.refreshImportBadge()
        SidebarManager.setupKeyboardShortcuts()
    }

    static onNavigate() {
        document.body.classList.remove('review-mode')
        SidebarManager.closeSidebar()
        SidebarManager.setupActiveLink()
        SidebarManager.setupAdvancedNav()
        SidebarManager.renderDataIcons()
        InputMasks.autoInit()
        NumberSpinner.autoInit()
        SidebarManager.initDatePickers()
        SidebarManager.initTranslations()
        SearchManager.invalidateCache()
        SearchManager.reset()
        SidebarManager.checkAchievements()
        SidebarManager.checkGoalCompletions()
        SidebarManager.refreshNotificationBadge()
        SidebarManager.refreshImportBadge()
    }

    static initTranslations() {
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key    = el.dataset.i18n
            const params = el.dataset.i18nParams ? JSON.parse(el.dataset.i18nParams) : undefined
            const text   = I18n.t(key, params)
            if (el.tagName === 'A') {
                const icon = el.querySelector('svg, i.ph')
                el.innerHTML = icon ? icon.outerHTML : ''
                el.appendChild(document.createTextNode(text))
            } else if (el.tagName === 'OPTION') {
                el.textContent = text
            } else if (el.tagName === 'SPAN' && el.parentElement?.classList.contains('radio-option')) {
                el.textContent = text
            } else if (el.querySelector('[data-i18n]')) {
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
            const titleKey   = el.dataset.i18nTitle
            const titleParam = el.dataset.i18nTitleParam
            const params     = titleParam ? { item: I18n.t(titleParam) } : undefined
            const text       = I18n.t(titleKey, params)
            if (el.classList.contains('info-hint-btn')) {
                el.dataset.tooltip = text
            } else {
                el.title = text
            }
        })

        document.querySelectorAll('[data-i18n-aria]').forEach(el => {
            el.setAttribute('aria-label', I18n.t(el.dataset.i18nAria))
        })

        document.querySelectorAll('.detail-empty').forEach(el => {
            el.textContent = I18n.t('commonNotInformed')
        })

        CustomSelect.syncAll()

        document.querySelectorAll('label[for]').forEach(label => {
            const input = document.getElementById(label.htmlFor)
            label.querySelectorAll('.required-mark, .optional-label').forEach(m => m.remove())
            if (!input) return
            if (input.required) {
                const mark = document.createElement('span')
                mark.className = 'required-mark'
                mark.textContent = ' *'
                label.appendChild(mark)
            } else if ('optionalLabel' in label.dataset) {
                const mark = document.createElement('span')
                mark.className = 'optional-label'
                mark.textContent = ' ' + I18n.t('commonOptional')
                label.appendChild(mark)
            }
        })

        ThemeManager.updateToggleIcon()
    }

    static updateDatePickerLocales() {
        if (typeof flatpickr === 'undefined') return
        const lang      = I18n.getLanguage()
        const localeKey = FLATPICKR_LOCALES[lang]
        const locale    = localeKey ? (flatpickr.l10ns?.[localeKey] ?? flatpickr.l10ns.default) : flatpickr.l10ns.default
        const altFormat = lang === 'en' ? 'm/d/Y' : 'd/m/Y'
        document.querySelectorAll('input[data-fp-init]').forEach(input => {
            input._flatpickr?.set('locale', locale)
            input._flatpickr?.set('altFormat', altFormat)
        })
    }

    static initDatePickers() {
        if (typeof flatpickr === 'undefined') return
        const lang   = I18n.getLanguage()
        const locale = FLATPICKR_LOCALES[lang]
            ? (flatpickr.l10ns?.[FLATPICKR_LOCALES[lang]] ?? undefined)
            : undefined

        const altFormat = lang === 'en' ? 'm/d/Y' : 'd/m/Y'
        document.querySelectorAll('input[type="date"]:not([data-fp-init])').forEach(input => {
            input.dataset.fpInit = '1'
            const fp = flatpickr(input, {
                dateFormat:     'Y-m-d',
                altInput:       true,
                altFormat,
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
            SidebarManager._attachWheelMonthNav(fp)
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

    static _attachWheelMonthNav(fp) {
        fp.calendarContainer.addEventListener('wheel', e => {
            if (e.target.closest('.cs-wrapper, select')) return
            e.preventDefault()
            fp.changeMonth(e.deltaY > 0 ? 1 : -1)
        }, { passive: false })
    }

    static checkAuth() {
        const pendingUrl = sessionStorage.getItem('__spa_url') ?? ''
        if (pendingUrl.includes('?guest=true')) return

        $.ajax({
            url: '/api/auth/me',
            type: 'GET',
            async: false,
            success: function (user) { globalThis.__currentUser = user },
            error: function (xhr) {
                if (xhr.status === 401) globalThis.location.href = '/pages/Login.html'
            }
        })
    }

    static applyAdminVisibility() {
        const isAdmin = !!globalThis.__currentUser?.admin
        const link    = document.getElementById('admin-link')
        const section = document.getElementById('admin-section')
        if (link)    link.hidden    = !isAdmin
        if (section) section.hidden = !isAdmin
    }

    static refreshImportBadge() {
        const hasReview = !!sessionStorage.getItem('__statementReview')
        const link = document.querySelector('.sidebar-link[href*="StatementImport.html"]')
        if (link) link.classList.toggle('has-import-progress', hasReview)
    }

    static refreshNotificationBadge() {
        const pendingUrl = sessionStorage.getItem('__spa_url') ?? ''
        if (pendingUrl.includes('?guest=true')) return
        try {
            let result = null
            $.ajax({ url: '/api/notifications/unread-count', type: 'GET', async: false, success: d => { result = d } })
            const badge = document.getElementById('notification-badge')
            if (!badge || result === null) return
            const count = result.count ?? 0
            badge.textContent = count > 99 ? '99+' : String(count)
            badge.style.display = count > 0 ? '' : 'none'
        } catch {}
    }

    static renderIcons() {
        const iconMap = {
            'HomePage.html': 'home',
            'Dashboard.html': 'dashboard',
            'Reports.html': 'reports',
            'Budget.html': 'budget',
            'GoalList.html': 'goals',
            'Transaction.html': 'transaction',
            'Transfer.html': 'transfer',
            'AccountList.html': 'accounts',
            'CategoryList.html': 'categories',
            'FinancialInstitutionList.html': 'institutions',
            'TransactionLocaleList.html': 'locations',
            'StatementImport.html': 'statementImport',
            'AchievementList.html': 'achievements',
            'FinnyCenter.html':    'finny',
            'Feedback.html':      'feedback',
            'Admin.html':         'adminPanel',
            'FeedbackAdmin.html': 'adminPanel',
            'UserView.html':      'profile'
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

    static setupAdvancedNav() {
        const toggle = document.getElementById('advanced-nav-toggle')
        const panel  = document.getElementById('advanced-nav')
        if (!toggle || !panel) return

        const setOpen = (open, persist = true) => {
            panel.hidden = !open
            toggle.setAttribute('aria-expanded', String(open))
            toggle.classList.toggle('sidebar-advanced-toggle--open', open)
            if (persist) { try { localStorage.setItem('sidebar_advanced_open', open ? '1' : '0') } catch { } }
        }

        if (!toggle.dataset.bound) {
            toggle.dataset.bound = '1'
            toggle.addEventListener('click', () => setOpen(panel.hidden, true))
        }

        const currentPage  = location.pathname.split('/').pop()
        const onAdvancedPage = [...panel.querySelectorAll('a[href]')]
            .some(a => a.href.split('/').pop() === currentPage)
        let persisted = false
        try { persisted = localStorage.getItem('sidebar_advanced_open') === '1' } catch { }
        setOpen(onAdvancedPage || persisted, false)
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

    static closeSidebar() {
        document.getElementById('sidebar')?.classList.remove('open')
        document.getElementById('sidebar-overlay')?.classList.remove('show')
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

    static checkGoalCompletions() {
        const pendingUrl = sessionStorage.getItem('__spa_url') ?? ''
        if (pendingUrl.includes('?guest=true')) return
        try {
            let goals = null
            $.ajax({ url: '/api/goals', type: 'GET', async: false, success: data => { goals = data } })
            if (!goals) return
            const TOAST_KEY = 'goalsCompletedSeen'
            const seen      = new Set(JSON.parse(localStorage.getItem(TOAST_KEY) ?? '[]'))
            const newlyDone = goals.filter(g => g.status === 'completed' && !seen.has(g.id))
            for (const g of newlyDone) {
                showToast(`🎯 ${I18n.t('goalCompletedToast')}: ${g.name}`, 'success', {
                    label: I18n.t('commonView'),
                    url:   `/pages/lists/GoalList.html?highlight=${g.id}`
                })
            }
            const allDone = goals.filter(g => g.status === 'completed').map(g => g.id)
            localStorage.setItem(TOAST_KEY, JSON.stringify(allDone))
        } catch {}
    }

    static setupKeyboardShortcuts() {
        const QWERTY_ROW = ['KeyQ','KeyW','KeyE','KeyR','KeyT','KeyY','KeyU','KeyI','KeyO','KeyP']

        document.addEventListener('keydown', e => {
            if (!e.altKey) return
            const tag = document.activeElement?.tagName
            if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return
            if (document.activeElement?.isContentEditable) return

            let linkIdx
            if (e.code.startsWith('Digit')) {
                const digit = e.code.slice(5)
                linkIdx = digit === '0' ? 9 : Number(digit) - 1
            } else {
                const qIdx = QWERTY_ROW.indexOf(e.code)
                if (qIdx === -1) return
                linkIdx = 10 + qIdx
            }

            const links = [
                ...document.querySelectorAll('.sidebar-nav .sidebar-link[href]'),
                ...document.querySelectorAll('.sidebar-footer .sidebar-link[href]'),
            ]
            const link = links[linkIdx]
            if (!link) return

            e.preventDefault()
            navigate(link.getAttribute('href'))
        })
    }

    static checkAchievements() {
        const pendingUrl = sessionStorage.getItem('__spa_url') ?? ''
        if (pendingUrl.includes('?guest=true')) return
        try {
            let achievements = null
            $.ajax({ url: '/api/achievements', type: 'GET', async: false, success: data => { achievements = data } })
            if (!achievements) return
            const TOAST_KEY = 'achievementsShownToast'
            const seen      = new Set(JSON.parse(localStorage.getItem(TOAST_KEY) ?? '[]'))
            const newOnes   = achievements.filter(a => a.earned && !seen.has(a.type))
            for (const a of newOnes) {
                const title = I18n.t(`achievement_${a.type}_title`)
                showToast(`🏆 ${I18n.t('achievementUnlocked')}: ${title}`, 'success', {
                    label: I18n.t('commonView'),
                    url:   `/pages/lists/AchievementList.html?highlight=${a.type}`
                })
            }
            const allEarned = achievements.filter(a => a.earned).map(a => a.type)
            localStorage.setItem(TOAST_KEY, JSON.stringify(allEarned))
        } catch {}
    }
}
