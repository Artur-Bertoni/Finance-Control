import { doRequest, formatCurrency, navigate, setBreadcrumb, showConfirm, showToast } from '../utils/FrontendFunctions.js'
import { Transaction } from './class/TransactionClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()

    const transactionId = new URLSearchParams(globalThis.location.search).get('id')
    if (!transactionId) { navigate('/pages/HomePage.html'); return }

    const response = doRequest(`/api/transactions/${transactionId}`, 'GET')
    if (!response?.id) { navigate('/pages/HomePage.html'); return }

    const tx = Transaction.processTransaction(response)

    const label = Transaction.formatLabel(tx)

    setBreadcrumb([
        { i18nKey: 'movements', url: '/pages/HomePage.html' },
        { label }
    ])

    document.getElementById('detail-account').textContent  = tx.account
    document.getElementById('detail-category').textContent = tx.category

    const isInstallment = tx.type === 'debit' && tx.installmentsNumber > 0
    const typeClass     = isInstallment ? 'installment' : tx.type
    const typeLabel     = isInstallment ? I18n.t('installment') : (tx.type === 'debit' ? I18n.t('debit') : I18n.t('credit'))
    const typeEl        = document.getElementById('detail-type')
    typeEl.textContent  = typeLabel
    typeEl.className    = `tx-badge ${typeClass}`

    const d = new Date(tx.date)
    document.getElementById('detail-date').textContent =
        `${d.getUTCDate().toString().padStart(2,'0')}/${(d.getUTCMonth()+1).toString().padStart(2,'0')}/${d.getUTCFullYear()}`

    const valueEl = document.getElementById('detail-value')
    valueEl.className = `detail-balance ${tx.type === 'credit' ? 'positive' : 'negative'}`
    valueEl.textContent = tx.type === 'debit' ? `- $ ${formatCurrency(tx.value)}` : `+ $ ${formatCurrency(tx.value)}`

    const installField = document.getElementById('detail-installments-field')
    if (tx.installmentsNumber > 0) {
        document.getElementById('detail-installments').textContent = tx.installmentsNumber
    } else {
        installField.style.display = 'none'
    }

    const localeEl = document.getElementById('detail-locale')
    if (tx.transactionLocale) {
        localeEl.textContent = tx.transactionLocale
    } else {
        localeEl.innerHTML = `<span class="detail-empty">${I18n.t('notInformed')}</span>`
    }

    const obsEl = document.getElementById('detail-obs')
    if (tx.obs) {
        obsEl.textContent = tx.obs
    } else {
        obsEl.innerHTML = `<span class="detail-empty">${I18n.t('notInformed')}</span>`
    }

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate(`/pages/Transaction.html?id=${transactionId}`)
    )

    document.getElementById('delete-btn').addEventListener('click', () => {
        showConfirm(I18n.t('deleteConfirm'), () => {
            $.ajax({
                url:   `/api/transactions/${transactionId}`,
                type:  'DELETE',
                async: false,
                success: () => navigate('/pages/HomePage.html'),
                error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingTransaction'), 'error')
            })
        })
    })
}

if (!globalThis.__appRouter) init()
