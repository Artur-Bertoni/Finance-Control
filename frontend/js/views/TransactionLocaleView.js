import { doRequest, navigate, setBreadcrumb } from '../../utils/FrontendFunctions.js'
import { TransactionLocale } from '../class/TransactionLocaleClass.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { I18n } from '../i18n.js'

export function init() {
    SidebarManager.initialize()

    const localeId = new URLSearchParams(globalThis.location.search).get('id')
    if (!localeId) { navigate('/pages/lists/TransactionLocaleList.html'); return }

    const response = doRequest(`/api/transaction-locales/${localeId}`, 'GET')
    if (!response?.id) { navigate('/pages/lists/TransactionLocaleList.html'); return }

    const loc = TransactionLocale.processTransactionLocale(response)

    setBreadcrumb([
        { i18nKey: 'locations', url: '/pages/lists/TransactionLocaleList.html' },
        { label: loc.name }
    ])

    document.getElementById('detail-name').textContent = loc.name

    if (loc.iconKey) {
        const iconRow = document.getElementById('icon-row')
        const iconEl  = document.getElementById('detail-icon')
        if (iconRow) iconRow.style.display = ''
        if (iconEl)  iconEl.innerHTML = `<i class="ph ${loc.iconKey}"></i>`
    }

    const addressEl = document.getElementById('detail-address')
    if (loc.address) {
        addressEl.textContent = loc.address
    } else {
        addressEl.innerHTML = `<span class="detail-empty">${I18n.t('commonNotInformed')}</span>`
    }

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate(`/pages/crud/TransactionLocale.html?id=${localeId}`)
    )
}

if (!globalThis.__appRouter) init()
