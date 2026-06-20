import { doRequest, formatMoney, formatDate, navigate, setBreadcrumb, showQuickAdd, showToast } from '../../utils/FrontendFunctions.js'
import { Account } from '../class/AccountClass.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { I18n } from '../i18n.js'

export function init() {
    SidebarManager.initialize()

    const accountId = new URLSearchParams(globalThis.location.search).get('id')
    if (!accountId) { navigate('/pages/lists/AccountList.html'); return }

    const accResp = doRequest(`/api/accounts/${accountId}`, 'GET')
    if (!accResp?.id) { navigate('/pages/lists/AccountList.html'); return }
    const acc = Account.processAccount(accResp)

    setBreadcrumb([
        { i18nKey: 'accounts', url: '/pages/lists/AccountList.html' },
        { label: acc.name, url: `/pages/views/AccountView.html?id=${accountId}` },
        { i18nKey: 'invoicesTitle' }
    ])

    document.getElementById('invoices-account-name').textContent = acc.name

    loadInvoices(accountId)
}

function loadInvoices(accountId) {
    const list = document.getElementById('invoices-list')
    list.innerHTML = ''

    const invoices = doRequest(`/api/accounts/${accountId}/invoices`, 'GET') ?? []
    if (!invoices.length) {
        const empty = document.createElement('div')
        empty.className = 'empty-state'
        empty.innerHTML = `<p>${I18n.t('noInvoices')}</p>`
        list.appendChild(empty)
        return
    }

    for (const inv of invoices) list.appendChild(buildInvoiceRow(accountId, inv))
}

function buildInvoiceRow(accountId, inv) {
    const row = document.createElement('div')
    row.className = 'transaction-item'

    const info = document.createElement('div')
    info.className = 'tx-info'

    const title = document.createElement('div')
    title.className = 'tx-cat-name'
    title.textContent = formatReference(inv.referenceMonth)

    const meta = document.createElement('div')
    meta.className = 'tx-meta'
    meta.textContent = `${I18n.t('invoiceDue')}: ${formatDate(inv.dueDate)} · ${inv.itemCount} ${I18n.t('invoiceItems')}`

    info.appendChild(title)
    info.appendChild(meta)

    const badge = document.createElement('span')
    badge.className = 'tx-badge'
    badge.textContent = statusLabel(inv.status)

    const value = document.createElement('div')
    value.className = 'tx-value'
    value.textContent = formatMoney(inv.total)

    row.appendChild(info)
    row.appendChild(badge)
    row.appendChild(value)

    if (inv.status !== 'PAID' && inv.total > 0) {
        const payBtn = document.createElement('button')
        payBtn.className = 'btn btn-primary btn-sm'
        payBtn.textContent = I18n.t('payInvoice')
        payBtn.addEventListener('click', () => openPayModal(accountId, inv))
        row.appendChild(payBtn)
    }

    return row
}

function openPayModal(accountId, inv) {
    const accounts = (doRequest('/api/accounts', 'GET') ?? [])
        .filter(a => a.type !== 'CREDIT_CARD' && a.id !== Number(accountId))
        .map(a => ({ value: a.id, label: a.name }))

    const categories = (doRequest('/api/categories', 'GET') ?? [])
        .map(c => ({ value: c.id, label: c.name }))

    showQuickAdd({
        title:  `${I18n.t('payInvoice')} · ${formatReference(inv.referenceMonth)}`,
        apiUrl: `/api/accounts/${accountId}/invoices/${inv.referenceMonth}/pay`,
        successToast: false,
        fields: [
            { id: 'source',   label: `${I18n.t('paySource')} *`,   type: 'select', required: true, options: accounts,   placeholder: I18n.t('selectAccount') },
            { id: 'category', label: `${I18n.t('payCategory')} *`, type: 'select', required: true, options: categories, placeholder: I18n.t('selectCategory') }
        ],
        buildBody: v => ({ sourceAccountId: Number(v.source), categoryId: Number(v.category) }),
        onSuccess: paid => {
            loadInvoices(accountId)
            const action = paid?.paymentTransactionId
                ? { label: I18n.t('view'), url: `/pages/views/TransactionView.html?id=${paid.paymentTransactionId}` }
                : null
            showToast(I18n.t('invoicePaidSuccess'), 'success', action)
        }
    })
}

function statusLabel(status) {
    if (status === 'PAID') return I18n.t('invoiceStatusPaid')
    if (status === 'OPEN') return I18n.t('invoiceStatusOpen')
    return I18n.t('invoiceStatusClosed')
}

function formatReference(ref) {
    const [y, m] = ref.split('-')
    return `${m}/${y}`
}

if (!globalThis.__appRouter) init()
