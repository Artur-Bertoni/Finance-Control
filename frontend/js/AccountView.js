import { doRequest, navigate, setBreadcrumb, showConfirm, showToast } from '../utils/FrontendFunctions.js'
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
        { label: I18n.t('accounts'), url: '/pages/AccountDashboard.html' },
        { label: acc.name }
    ])

    document.getElementById('detail-name').textContent = acc.name

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
    balEl.textContent = `${acc.balance >= 0 ? '+' : '-'} $ ${Math.abs(acc.balance).toFixed(2)}`

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate(`/pages/Account.html?id=${accountId}`)
    )

    document.getElementById('delete-btn').addEventListener('click', () => {
        showConfirm(I18n.t('deleteConfirm'), () => {
            $.ajax({
                url:   `/api/accounts/${accountId}`,
                type:  'DELETE',
                async: false,
                success: () => navigate('/pages/AccountDashboard.html'),
                error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingAccount'), 'error')
            })
        })
    })
}

if (!globalThis.__appRouter) init()
