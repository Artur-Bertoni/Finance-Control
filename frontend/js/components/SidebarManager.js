import { Icons } from '../icons/IconLibrary.js'
import { ThemeManager } from '../ThemeManager.js'
import { CustomSelect } from './CustomSelect.js'

export class SidebarManager {
    static _initialized = false

    static initialize() {
        if (SidebarManager._initialized) return
        SidebarManager._initialized = true
        SidebarManager.checkAuth()
        SidebarManager.renderIcons()
        SidebarManager.renderDataIcons()
        SidebarManager.setupActiveLink()
        SidebarManager.setupToggleButton()
        SidebarManager.setupOverlayDismiss()
        ThemeManager.initialize()
        CustomSelect.autoInit()
        SidebarManager.initDatePickers()
    }

    static onNavigate() {
        SidebarManager.setupActiveLink()
        SidebarManager.renderDataIcons()
        SidebarManager.initDatePickers()
    }

    static initDatePickers() {
        if (typeof flatpickr === 'undefined') return
        document.querySelectorAll('input[type="date"]:not([data-fp-init])').forEach(input => {
            input.dataset.fpInit = '1'
            const fp = flatpickr(input, {
                dateFormat:     'Y-m-d',
                altInput:       true,
                altFormat:      'd/m/Y',
                altInputClass:  'flatpickr-input fc-date-input',
                maxDate:        'today',
                defaultDate:    input.value || 'today',
                locale:         flatpickr.l10ns?.pt,
                disableMobile:  true,
                allowInput:     false,
                onChange: (_, __, instance) => {
                    instance.element.dispatchEvent(new Event('change', { bubbles: true }))
                }
            })
            fp.calendarContainer.addEventListener('wheel', e => {
                e.preventDefault()
                fp.changeMonth(e.deltaY > 0 ? 1 : -1)
            }, { passive: false })
            // Sync programmatic .value = assignments to Flatpickr display
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
        $.ajax({
            url: '/api/auth/me',
            type: 'GET',
            async: false,
            error: function (xhr) {
                if (xhr.status === 401) globalThis.location.href = '/pages/Login.html'
            }
        })
    }

    /**
     * Renderiza os ícones da sidebar a partir da biblioteca centralizada
     */
    static renderIcons() {
        const iconMap = {
            'HomePage.html': 'home',
            'Transaction.html': 'transaction',
            'Transfer.html': 'transfer',
            'AccountDashboard.html': 'accounts',
            'CategoryDashboard.html': 'categories',
            'FinancialInstitutionDashboard.html': 'institutions',
            'TransactionLocaleDashboard.html': 'locations',
            'User.html': 'profile'
        }

        document.querySelectorAll('.sidebar-nav .sidebar-link, .sidebar-footer .sidebar-link').forEach(link => {
            const href = link.getAttribute('href')?.split('/').pop()
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

    /**
     * Renderiza ícones em elementos com atributo data-icon
     */
    static renderDataIcons() {
        document.querySelectorAll('[data-icon]').forEach(el => {
            const iconName = el.dataset.icon
            const icon = Icons[iconName]
            if (icon) el.innerHTML = icon()
        })
    }

    /**
     * Marca o link ativo baseado na URL atual
     */
    static setupActiveLink() {
        const links = document.querySelectorAll('.sidebar-link[href]')
        const currentPage = location.pathname.split('/').pop()

        links.forEach(link => {
            const href = link.href.split('/').pop()
            link.classList.toggle('active', href === currentPage)
        })
    }

    /**
     * Configura o botão de toggle da sidebar para mobile
     */
    static setupToggleButton() {
        const toggleButton = document.getElementById('sidebar-toggle-btn')
        const sidebar = document.getElementById('sidebar')
        const overlay = document.getElementById('sidebar-overlay')

        if (!toggleButton || !sidebar) {
            console.warn('Sidebar toggle button ou sidebar element não encontrados')
            return
        }

        toggleButton.addEventListener('click', () => {
            sidebar.classList.toggle('open')
            if (overlay) {
                overlay.classList.toggle('show')
            }
        })
    }

    /**
     * Permite fechar a sidebar ao clicar no overlay
     */
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
