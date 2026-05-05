import { doRequest, navigate } from '../utils/FrontendFunctions.js'
import { Transaction } from './class/TransactionClass.js'
import { Category } from './class/CategoryClass.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'

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

    const transactions = []
    try {
        for (const el of data) transactions.push(Transaction.processTransaction(el))
    } catch (e) {
        console.log('Erro ao carregar transações:', e)
    }

    if (transactions.length === 0) {
        list.innerHTML = `
            <div class="empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M2.273 5.625A4.483 4.483 0 015.25 4.5h13.5c1.141 0 2.183.425 2.977 1.125A3 3 0 0018.75 3H5.25a3 3 0 00-2.977 2.625zM2.273 8.625A4.483 4.483 0 015.25 7.5h13.5c1.141 0 2.183.425 2.977 1.125A3 3 0 0018.75 6H5.25a3 3 0 00-2.977 2.625zM5.25 9a3 3 0 00-3 3v6a3 3 0 003 3h13.5a3 3 0 003-3v-6a3 3 0 00-3-3H5.25z"/></svg>
                <p>Nenhuma transação no período</p>
            </div>`
        updateTotals(accountId, 0)
        return
    }

    let filteredTotal = 0

    for (const tx of transactions) {
        const isInstallment = tx.type === 'debit' && tx.installmentsNumber > 0
        const typeClass = isInstallment ? 'installment' : tx.type

        const item = document.createElement('div')
        item.className = `transaction-item ${typeClass}`
        item.addEventListener('click', () => navigate(`/pages/Transaction.html?id=${tx.id}`))

        const indicator = document.createElement('div')
        indicator.className = `tx-indicator ${typeClass}`

        const info = document.createElement('div')
        info.className = 'tx-info'

        const cat = document.createElement('div')
        cat.className = 'tx-category'
        cat.textContent = tx.category

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

        const d = new Date(tx.date)
        const dateSpan = document.createElement('span')
        dateSpan.textContent = `${d.getUTCDate().toString().padStart(2,'0')}/${(d.getUTCMonth()+1).toString().padStart(2,'0')}/${d.getUTCFullYear()}`
        meta.appendChild(dateSpan)

        if (tx.installmentsNumber > 0) {
            const instSpan = document.createElement('span')
            instSpan.textContent = `${tx.installmentsNumber}x`
            meta.appendChild(instSpan)
        }

        info.appendChild(cat)
        info.appendChild(meta)

        const badge = document.createElement('span')
        badge.className = `tx-badge ${typeClass}`
        badge.textContent = isInstallment ? 'Parcelado' : (tx.type === 'debit' ? 'Débito' : 'Crédito')

        const value = document.createElement('div')
        value.className = `tx-value ${typeClass}`
        if (tx.type === 'debit') {
            value.textContent = `- $ ${tx.value.toFixed(2)}`
            if (!isInstallment) filteredTotal -= tx.value
        } else {
            value.textContent = `+ $ ${tx.value.toFixed(2)}`
            filteredTotal += tx.value
        }

        item.appendChild(indicator)
        item.appendChild(info)
        item.appendChild(badge)
        item.appendChild(value)
        list.appendChild(item)
    }

    updateTotals(accountId, filteredTotal)
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
