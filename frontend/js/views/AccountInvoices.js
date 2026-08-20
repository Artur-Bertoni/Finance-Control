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

    loadInvoices(accountId, acc)
}

function loadInvoices(accountId, acc) {
    const list = document.getElementById('invoices-list')
    list.innerHTML = ''

    const invoices = doRequest(`/api/accounts/${accountId}/invoices`, 'GET') ?? []

    renderLimit(acc, invoices)

    if (!invoices.length) {
        const empty = document.createElement('div')
        empty.className = 'empty-state'
        empty.innerHTML = `<p>${I18n.t('noInvoices')}</p>`
        list.appendChild(empty)
        return
    }

    for (const inv of invoices) list.appendChild(buildInvoiceRow(accountId, inv, acc))
}

function renderLimit(acc, invoices) {
    const box = document.getElementById('invoices-limit')
    if (!box) return
    if (acc?.creditLimit == null) { box.style.display = 'none'; return }

    const used = invoices
        .filter(i => i.status !== 'PAID' && i.total > 0)
        .reduce((sum, i) => sum + i.total, 0)
    const available = acc.creditLimit - used

    box.style.cssText = 'display:flex;flex-wrap:wrap;gap:16px;padding:10px 4px 4px;color:var(--text-muted);font-size:14px'
    box.innerHTML =
        `<span>${I18n.t('creditLimit')}: <strong>${formatMoney(acc.creditLimit)}</strong></span>` +
        `<span>${I18n.t('limitUsed')}: <strong>${formatMoney(used)}</strong></span>` +
        `<span>${I18n.t('limitAvailable')}: <strong>${formatMoney(available)}</strong></span>`
}

function buildInvoiceRow(accountId, inv, acc) {
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
        const linkBtn = document.createElement('button')
        linkBtn.className = 'btn btn-secondary btn-sm'
        linkBtn.textContent = I18n.t('linkPayment')
        linkBtn.addEventListener('click', () => openLinkModal(accountId, inv, acc))
        row.appendChild(linkBtn)

        const payBtn = document.createElement('button')
        payBtn.className = 'btn btn-primary btn-sm'
        payBtn.textContent = I18n.t('payInvoice')
        payBtn.addEventListener('click', () => openPayModal(accountId, inv, acc))
        row.appendChild(payBtn)
    }

    return row
}

function openLinkModal(accountId, inv, acc) {
    const start = shiftDays(inv.dueDate, -40)
    const end   = shiftDays(inv.dueDate, 15)
    const txs = doRequest(`/api/transactions?startDate=${start}&endDate=${end}`, 'GET') ?? []

    const candidates = txs
        .filter(t => String(t.type).toLowerCase() === 'debit')
        .filter(t => t.account?.type !== 'CREDIT_CARD')
        .sort((a, b) => Math.abs(a.value - inv.total) - Math.abs(b.value - inv.total))
        .slice(0, 20)
        .map(t => ({
            value: t.id,
            label: `${formatDate(t.date)} · ${formatMoney(t.value)} · ${t.account?.name ?? ''}`
        }))

    if (!candidates.length) {
        showToast(I18n.t('noLinkableMovement'), 'warning')
        return
    }

    showQuickAdd({
        title:  `${I18n.t('linkPayment')} · ${formatReference(inv.referenceMonth)}`,
        apiUrl: `/api/accounts/${accountId}/invoices/${inv.referenceMonth}/reconcile`,
        successToast: false,
        fields: [
            { id: 'movement', label: `${I18n.t('selectMovement')} *`, type: 'select', required: true, options: candidates, placeholder: I18n.t('selectMovement') }
        ],
        buildBody: v => ({ paymentTransactionId: Number(v.movement) }),
        onSuccess: () => {
            loadInvoices(accountId, acc)
            showToast(I18n.t('invoicePaidSuccess'), 'success')
        }
    })
}

function shiftDays(isoDate, days) {
    const d = new Date(isoDate)
    d.setDate(d.getDate() + days)
    return d.toISOString().slice(0, 10)
}

function openPayModal(accountId, inv, acc) {
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
            { id: 'category', label: `${I18n.t('payCategory')} *`, type: 'select', required: true, options: categories, placeholder: I18n.t('selectCategory') },
            { id: 'date',     label: I18n.t('payDate'),            type: 'date',   value: inv.dueDate }
        ],
        buildBody: v => ({ sourceAccountId: Number(v.source), categoryId: Number(v.category), date: v.date || null }),
        onSuccess: paid => {
            loadInvoices(accountId, acc)
            const action = paid?.paymentTransactionId
                ? { label: I18n.t('commonView'), url: `/pages/views/TransactionView.html?id=${paid.paymentTransactionId}` }
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
