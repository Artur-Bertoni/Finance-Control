import { doRequest, navigate } from '../utils/FrontendFunctions.js'
import { Transaction } from './class/TransactionClass.js'
import { Category } from './class/CategoryClass.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'

// Inicializar sidebar
SidebarManager.initialize()

Category.addCategories('category-input')
Account.addAccounts('account-input')
configureFilters()
populateTransactionsList()

function configureFilters() {
    const startInput = document.getElementById('start-date-input')
    const endInput   = document.getElementById('end-date-input')
    const today      = new Date().toISOString().split('T')[0]

    endInput.max   = today
    endInput.value = today

    startInput.max = today
    const firstOfMonth = new Date()
    firstOfMonth.setDate(1)
    startInput.value = firstOfMonth.toISOString().split('T')[0]

    startInput.addEventListener('change', refresh)
    endInput.addEventListener('change', refresh)
    document.getElementById('category-input').addEventListener('change', refresh)
    document.getElementById('account-input').addEventListener('change', refresh)
}

function refresh() {
    const list = document.getElementById('last-transaction-list')
    list.innerHTML = ''
    populateTransactionsList()
}

function populateTransactionsList() {
    const startDate  = document.getElementById('start-date-input').value
    const endDate    = document.getElementById('end-date-input').value
    const categoryId = document.getElementById('category-input').value
    const accountId  = document.getElementById('account-input').value

    const params = new URLSearchParams({ startDate, endDate })
    if (categoryId) params.append('categoryId', categoryId)
    if (accountId)  params.append('accountId', accountId)

    const data = doRequest(`/api/transactions?${params.toString()}`, 'GET') ?? []
    const list = document.getElementById('last-transaction-list')
    list.innerHTML = ''

    const transactions = loadTransactions(data)

    if (transactions.length === 0) {
        list.innerHTML = `
            <div class="empty-state">
                ${Icons.wallet()}
                <p>Nenhuma transação no período</p>
            </div>`
        updateTotals(accountId, 0)
        return
    }

    const { filteredTotal } = renderTransactions(transactions, list)
    updateTotals(accountId, filteredTotal)
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

function renderTransactions(transactions, listElement) {
    let filteredTotal = 0

    for (const tx of transactions) {
        const item = createTransactionItem(tx)
        listElement.appendChild(item)
        filteredTotal += calculateTransactionValue(tx)
    }

    return { filteredTotal }
}

function createTransactionItem(tx) {
    const isInstallment = tx.type === 'debit' && tx.installmentsNumber > 0
    const typeClass = isInstallment ? 'installment' : tx.type

    const item = document.createElement('div')
    item.className = `transaction-item ${typeClass}`
    item.addEventListener('click', () => navigate(`/pages/Transaction.html?id=${tx.id}`))

    const indicator = document.createElement('div')
    indicator.className = `tx-indicator ${typeClass}`

    const info = createTransactionInfo(tx)
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

    const meta = createTransactionMeta(tx)

    info.appendChild(cat)
    info.appendChild(meta)

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

    const dateSpan = createDateSpan(tx.date)
    meta.appendChild(dateSpan)

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
    badge.textContent = isInstallment ? 'Parcelado' : (tx.type === 'debit' ? 'Débito' : 'Crédito')
    return badge
}

function createTransactionValue(tx, typeClass) {
    const value = document.createElement('div')
    value.className = `tx-value ${typeClass}`
    value.textContent = tx.type === 'debit' ? `- $ ${tx.value.toFixed(2)}` : `+ $ ${tx.value.toFixed(2)}`
    return value
}

function calculateTransactionValue(tx) {
    const isInstallment = tx.type === 'debit' && tx.installmentsNumber > 0
    if (tx.type === 'debit') {
        return isInstallment ? 0 : -tx.value
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
