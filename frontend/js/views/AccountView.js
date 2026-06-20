import { doRequest, formatMoney, navigate, setBreadcrumb } from '../../utils/FrontendFunctions.js'
import { Account } from '../class/AccountClass.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { ChangeHistoryManager } from '../components/ChangeHistoryManager.js'
import { I18n } from '../i18n.js'

export function init() {
    SidebarManager.initialize()

    const accountId = new URLSearchParams(globalThis.location.search).get('id')
    if (!accountId) { navigate('/pages/lists/AccountList.html'); return }

    const response = doRequest(`/api/accounts/${accountId}`, 'GET')
    if (!response?.id) { navigate('/pages/lists/AccountList.html'); return }

    const acc = Account.processAccount(response)

    setBreadcrumb([
        { i18nKey: 'accounts', url: '/pages/lists/AccountList.html' },
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
        acc.financialInstitution || I18n.t('commonNotInformed')

    const contactEl = document.getElementById('detail-contact')
    if (acc.contact) {
        contactEl.textContent = acc.contact
    } else {
        contactEl.innerHTML = `<span class="detail-empty">${I18n.t('commonNotInformed')}</span>`
    }

    const descEl = document.getElementById('detail-description')
    if (acc.description) {
        descEl.textContent = acc.description
    } else {
        descEl.innerHTML = `<span class="detail-empty">${I18n.t('commonNotInformed')}</span>`
    }

    const isCreditCard = acc.type === 'CREDIT_CARD'
    document.getElementById('detail-type').textContent =
        I18n.t(isCreditCard ? 'accountTypeCreditCard' : 'accountTypeChecking')

    if (isCreditCard) {
        document.getElementById('closing-day-row').style.display = ''
        document.getElementById('due-day-row').style.display     = ''
        document.getElementById('detail-closing-day').textContent = acc.closingDay ?? I18n.t('commonNotInformed')
        document.getElementById('detail-due-day').textContent     = acc.dueDay ?? I18n.t('commonNotInformed')
    }

    const balEl = document.getElementById('detail-balance')
    let balClass = 'zero'
    if (acc.balance > 0) balClass = 'positive'
    else if (acc.balance < 0) balClass = 'negative'
    balEl.className = `detail-balance ${balClass}`
    balEl.textContent = `${acc.balance >= 0 ? '+' : '-'} ${formatMoney(Math.abs(acc.balance))}`

    if (acc.type === 'CREDIT_CARD') {
        const invBtn = document.getElementById('view-invoices-btn')
        if (invBtn) {
            invBtn.style.display = ''
            invBtn.addEventListener('click', () => navigate(`/pages/views/AccountInvoices.html?id=${accountId}`))
        }
    }

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate(`/pages/crud/Account.html?id=${accountId}`)
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

    let historyLoaded = false
    document.querySelectorAll('.view-tab').forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab
            document.querySelectorAll('.view-tab').forEach(b => b.classList.remove('view-tab--active'))
            btn.classList.add('view-tab--active')
            document.getElementById('tab-details').style.display = tab === 'details' ? '' : 'none'
            document.getElementById('tab-history').style.display  = tab === 'history'  ? '' : 'none'
            if (tab === 'history' && !historyLoaded) {
                historyLoaded = true
                ChangeHistoryManager.loadAndRender('account', accountId, response.createdAt, 'history-container')
            }
        })
    })
}

if (!globalThis.__appRouter) init()
