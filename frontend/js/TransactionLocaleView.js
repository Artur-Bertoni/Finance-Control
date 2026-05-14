import { doRequest, navigate, setBreadcrumb } from '../utils/FrontendFunctions.js'
import { TransactionLocale } from './class/TransactionLocaleClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()

    const localeId = new URLSearchParams(globalThis.location.search).get('id')
    if (!localeId) { navigate('/pages/TransactionLocaleDashboard.html'); return }

    const response = doRequest(`/api/transaction-locales/${localeId}`, 'GET')
    if (!response?.id) { navigate('/pages/TransactionLocaleDashboard.html'); return }

    const loc = TransactionLocale.processTransactionLocale(response)

    setBreadcrumb([
        { i18nKey: 'locations', url: '/pages/TransactionLocaleDashboard.html' },
        { label: loc.name }
    ])

    document.getElementById('detail-name').textContent = loc.name

    const addressEl = document.getElementById('detail-address')
    if (loc.address) {
        addressEl.textContent = loc.address
    } else {
        addressEl.innerHTML = `<span class="detail-empty">${I18n.t('notInformed')}</span>`
    }

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate(`/pages/TransactionLocale.html?id=${localeId}`)
    )
}

if (!globalThis.__appRouter) init()
