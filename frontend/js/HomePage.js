import { doRequest, navigate, showPendingToast } from '../utils/FrontendFunctions.js'
import { Transaction } from './class/TransactionClass.js'
import { Category } from './class/CategoryClass.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { CustomSelect } from './components/CustomSelect.js'
import { Icons } from './icons/IconLibrary.js'
import { I18n } from './i18n.js'

const PAGE_SIZE = 30
let allTransactions = []
let currentPage = 1

export function init() {
    allTransactions = []
    currentPage = 1
    document.body.classList.add('page-home')
    SidebarManager.initialize()
    showPendingToast()
    Category.addCategories('category-input')
    Account.addAccounts('account-input')
    configureFilters()
    populateTransactionsList()

    I18n.onChange(() => renderPage())
}

function configureFilters() {
    const startInput = document.getElementById('start-date-input')
    const endInput   = document.getElementById('end-date-input')
    const today = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().split('T')[0]

    endInput.max   = today
    endInput.value = today
    startInput.max = today

    const firstOfMonth = new Date()
    firstOfMonth.setDate(1)
    startInput.value = new Date(firstOfMonth.getTime() - firstOfMonth.getTimezoneOffset() * 60000).toISOString().split('T')[0]
    startInput.addEventListener('change', refresh)
    endInput.addEventListener('change', refresh)

    document.getElementById('category-input').addEventListener('change', refresh)
    document.getElementById('account-input').addEventListener('change', refresh)
}

function refresh() {
    currentPage = 1
    populateTransactionsList()
}

function populateTransactionsList() {
    const startDate  = document.getElementById('start-date-input').value
    const endDate    = document.getElementById('end-date-input').value
    const categoryId = document.getElementById('category-input').value
    const accountId  = document.getElementById('account-input').value

    const params = new URLSearchParams({ startDate, endDate })
    if (categoryId) params.append('categoryId', categoryId)
    if (accountId)  params.append('accountId',  accountId)

    const data = doRequest(`/api/transactions?${params.toString()}`, 'GET') ?? []
    allTransactions = loadTransactions(data)

    const filteredTotal = allTransactions.reduce((sum, tx) => sum + calculateTransactionValue(tx), 0)
    updateTotals(accountId, filteredTotal)

    renderPage()
}

function loadTransactions(data) {
    const transactions = []
    try {
        for (const el of data) transactions.push(Transaction.processTransaction(el))
    } catch (e) {
        console.log('Erro ao carregar transações:', e)
    }
    return transactions
}

function renderPage() {
    const list = document.getElementById('last-transaction-list')
    if (!list) return
    list.innerHTML = ''
    list.scrollTop = 0

    const existing = document.getElementById('transactions-pagination')
    if (existing) existing.remove()

    if (allTransactions.length === 0) {
        list.innerHTML = `
            <div class="empty-state">
                ${Icons.wallet()}
                <p>${I18n.t('noTransactionsInPeriod')}</p>
            </div>`
        return
    }

    const start     = (currentPage - 1) * PAGE_SIZE
    const pageItems = allTransactions.slice(start, start + PAGE_SIZE)
    for (const tx of pageItems) list.appendChild(createTransactionItem(tx))

    renderPagination()
}

function renderPagination() {
    const totalPages = Math.max(1, Math.ceil(allTransactions.length / PAGE_SIZE))

    const pag = document.createElement('div')
    pag.id = 'transactions-pagination'
    pag.className = 'pagination'

    const prevBtn = document.createElement('button')
    prevBtn.className = 'pagination-btn'
    prevBtn.textContent = '‹'
    prevBtn.disabled = currentPage === 1
    prevBtn.addEventListener('click', () => { currentPage--; renderPage() })
    pag.appendChild(prevBtn)

    const select = document.createElement('select')
    select.className = 'pagination-select'
    for (let i = 1; i <= totalPages; i++) {
        const opt = document.createElement('option')
        opt.value = i
        opt.textContent = I18n.t('pageOf', { page: i, total: totalPages })
        opt.selected = i === currentPage
        select.appendChild(opt)
    }
    select.addEventListener('change', () => { currentPage = Number(select.value); renderPage() })
    pag.appendChild(select)
    CustomSelect.wrap(select)

    const nextBtn = document.createElement('button')
    nextBtn.className = 'pagination-btn'
    nextBtn.textContent = '›'
    nextBtn.disabled = currentPage === totalPages
    nextBtn.addEventListener('click', () => { currentPage++; renderPage() })
    pag.appendChild(nextBtn)

    const rangeStart = (currentPage - 1) * PAGE_SIZE + 1
    const rangeEnd   = Math.min(currentPage * PAGE_SIZE, allTransactions.length)
    const info = document.createElement('span')
    info.className = 'pagination-info'
    info.textContent = I18n.t('paginationInfo', { start: rangeStart, end: rangeEnd, total: allTransactions.length })
    pag.appendChild(info)

    document.querySelector('.transactions-card .card-header').appendChild(pag)
}

function createTransactionItem(tx) {
    const isInstallment = tx.type === 'debit' && tx.installmentsNumber > 0
    const typeClass     = isInstallment ? 'installment' : tx.type

    const item = document.createElement('div')
    item.className = `transaction-item ${typeClass}`
    item.addEventListener('click', () => navigate(`/pages/TransactionView.html?id=${tx.id}`))

    const indicator = document.createElement('div')
    indicator.className = `tx-indicator ${typeClass}`

    const info  = createTransactionInfo(tx)
    const badge = createTransactionBadge(tx, isInstallment, typeClass)
    const value = createTransactionValue(tx, typeClass)

    item.appendChild(indicator)
    item.appendChild(info)
    item.appendChild(badge)
    item.appendChild(value)

    return item
}

function createTransactionInfo(tx) {
    const info = document.createElement('div')
    info.className = 'tx-info'

    const cat = document.createElement('div')
    cat.className = 'tx-category'
    cat.textContent = tx.category

    info.appendChild(cat)
    info.appendChild(createTransactionMeta(tx))
    return info
}

function createTransactionMeta(tx) {
    const meta = document.createElement('div')
    meta.className = 'tx-meta'

    const accSpan = document.createElement('span')
    accSpan.textContent = tx.account
    meta.appendChild(accSpan)

    if (tx.transactionLocale) {
        const locSpan = document.createElement('span')
        locSpan.textContent = tx.transactionLocale
        meta.appendChild(locSpan)
    }

    meta.appendChild(createDateSpan(tx.date))

    if (tx.installmentsNumber > 0) {
        const instSpan = document.createElement('span')
        instSpan.textContent = `${tx.installmentsNumber}x`
        meta.appendChild(instSpan)
    }

    return meta
}

function createDateSpan(date) {
    const d = new Date(date)
    const dateSpan = document.createElement('span')
    dateSpan.textContent = `${d.getUTCDate().toString().padStart(2,'0')}/${(d.getUTCMonth()+1).toString().padStart(2,'0')}/${d.getUTCFullYear()}`
    return dateSpan
}

function createTransactionBadge(tx, isInstallment, typeClass) {
    const badge = document.createElement('span')
    badge.className = `tx-badge ${typeClass}`
    badge.textContent = isInstallment ? I18n.t('installment') : (tx.type === 'debit' ? I18n.t('debit') : I18n.t('credit'))
    return badge
}

function createTransactionValue(tx, typeClass) {
    const value = document.createElement('div')
    value.className = `tx-value ${typeClass}`
    value.textContent = tx.type === 'debit' ? `- $ ${tx.value.toFixed(2)}` : `+ $ ${tx.value.toFixed(2)}`
    return value
}

function calculateTransactionValue(tx) {
    if (tx.type === 'debit') {
        return (tx.installmentsNumber > 0) ? 0 : -tx.value
    }
    return tx.value
}

function updateTotals(accountId, filteredTotal) {
    const totalParams = new URLSearchParams()
    if (accountId) totalParams.append('accountId', accountId)
    const totalValue = doRequest(`/api/accounts/total-value?${totalParams.toString()}`, 'GET') ?? 0
    const totalNum   = Number(totalValue)

    const totalBox    = document.getElementById('home-total-box')
    const filteredBox = document.getElementById('filtered-total-box')

    totalBox.textContent    = `$ ${totalNum.toFixed(2)}`
    totalBox.className      = 'stat-card-value ' + (totalNum >= 0 ? 'positive' : 'negative')

    filteredBox.textContent = `$ ${filteredTotal.toFixed(2)}`
    filteredBox.className   = 'stat-card-value ' + (filteredTotal >= 0 ? 'positive' : 'negative')
}

if (!globalThis.__appRouter) init()
