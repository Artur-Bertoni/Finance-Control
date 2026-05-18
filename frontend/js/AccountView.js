import { doRequest, formatCurrency, navigate, setBreadcrumb } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()

    const accountId = new URLSearchParams(globalThis.location.search).get('id')
    if (!accountId) { navigate('/pages/AccountDashboard.html'); return }

    const response = doRequest(`/api/accounts/${accountId}`, 'GET')
    if (!response?.id) { navigate('/pages/AccountDashboard.html'); return }

    const acc = Account.processAccount(response)

    setBreadcrumb([
        { i18nKey: 'accounts', url: '/pages/AccountDashboard.html' },
        { label: acc.name }
    ])

    document.getElementById('detail-name').textContent = acc.name

    if (acc.iconKey) {
        const iconRow = document.getElementById('icon-row')
        const iconEl  = document.getElementById('detail-icon')
        if (iconRow) iconRow.style.display = ''
        if (iconEl)  iconEl.innerHTML = `<i class="ph ${acc.iconKey}"></i>`
    }

    document.getElementById('detail-institution').textContent =
        acc.financialInstitution || I18n.t('notInformed')

    const contactEl = document.getElementById('detail-contact')
    if (acc.contact) {
        contactEl.textContent = acc.contact
    } else {
        contactEl.innerHTML = `<span class="detail-empty">${I18n.t('notInformed')}</span>`
    }

    const descEl = document.getElementById('detail-description')
    if (acc.description) {
        descEl.textContent = acc.description
    } else {
        descEl.innerHTML = `<span class="detail-empty">${I18n.t('notInformed')}</span>`
    }

    const balEl = document.getElementById('detail-balance')
    let balClass = 'zero'
    if (acc.balance > 0) balClass = 'positive'
    else if (acc.balance < 0) balClass = 'negative'
    balEl.className = `detail-balance ${balClass}`
    balEl.textContent = `${acc.balance >= 0 ? '+' : '-'} $ ${formatCurrency(Math.abs(acc.balance))}`

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate(`/pages/Account.html?id=${accountId}`)
    )

    document.getElementById('view-transactions-btn')?.addEventListener('click', () => {
        const today = new Date()
        const firstOfYear = new Date(today.getFullYear(), 0, 1)
        const toStr = d => new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().split('T')[0]
        sessionStorage.setItem('__homeFilters', JSON.stringify({
            account:   accountId,
            startDate: toStr(firstOfYear),
            endDate:   toStr(today),
        }))
        navigate('/pages/HomePage.html')
    })
}

if (!globalThis.__appRouter) init()
