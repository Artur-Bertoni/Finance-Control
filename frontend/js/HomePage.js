import {doRequest, navigate} from "../utils/FrontendFunctions.js"
import {Transaction} from "./class/TransactionClass.js"
import {Category} from "./class/CategoryClass.js"
import {Account} from "./class/AccountClass.js"

configureFilters()
populateTransactionsList()
Category.addCategories('category-input')
Account.addAccounts('account-input')

document.getElementById('dashboard-form').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'postTransaction') navigate('/pages/Transaction.html')
    else if (name === 'transactionLocales') navigate('/pages/TransactionLocaleDashboard.html')
    else if (name === 'financialInstitutions') navigate('/pages/FinancialInstitutionDashboard.html')
    else if (name === 'accounts') navigate('/pages/AccountDashboard.html')
    else if (name === 'categories') navigate('/pages/CategoryDashboard.html')
})

function populateTransactionsList() {
    let transactions = []

    let startDate = document.getElementById('start-date-input').value
    let endDate = document.getElementById('end-date-input').value
    let categoryId = document.getElementById('category-input').value
    let accountId = document.getElementById('account-input').value

    let params = new URLSearchParams({ startDate, endDate })
    if (categoryId) params.append('categoryId', categoryId)
    if (accountId) params.append('accountId', accountId)

    let data = doRequest(`/api/transactions?${params.toString()}`, 'GET')

    try {
        for (const element of (data ?? [])) {
            transactions.push(Transaction.processTransaction(element))
        }
    } catch (e) {
        console.log('No transactions recovered from DB: ' + e)
    }

    let list = document.getElementById('last-transaction-list')
    let filteredTotal = 0

    for (const element of transactions) {
        let button = document.createElement('button')
        button.classList.add('dashboard-item-btn')
        button.classList.add('colorful-button')
        button.type = 'button'
        button.addEventListener('click', () => navigate(`/pages/Transaction.html?id=${element.id}`))

        if (element.type === 'debit' && element.installmentsNumber === 0)
            button.style.backgroundColor = '#C04C4CCC'
        else if (element.type === 'debit' && element.installmentsNumber > 0)
            button.style.backgroundColor = '#0085b6'
        else
            button.style.backgroundColor = '#4BAE50FF'

        let grid = document.createElement('div')
        grid.classList.add('dashboard-grid')

        let categoryLabel = document.createElement('span')
        categoryLabel.classList.add('grid-label')
        categoryLabel.innerText = `Categoria: ${element.category}`
        grid.appendChild(categoryLabel)

        let accountLabel = document.createElement('span')
        accountLabel.classList.add('grid-label')
        accountLabel.innerText = `Conta: ${element.account}`
        grid.appendChild(accountLabel)

        let localeLabel = document.createElement('span')
        localeLabel.classList.add('grid-label')
        localeLabel.innerText = element.transactionLocale ? `Local: ${element.transactionLocale}` : 'Local: Não Informado'
        grid.appendChild(localeLabel)

        let dateLabel = document.createElement('span')
        dateLabel.classList.add('grid-label')
        let date = new Date(element.date)
        dateLabel.innerText = `Data: ${date.getUTCDate().toString().padStart(2, '0')}/${(date.getUTCMonth() + 1).toString().padStart(2, '0')}/${date.getUTCFullYear()}`
        grid.appendChild(dateLabel)

        let valueLabel = document.createElement('span')
        valueLabel.classList.add('grid-label')
        if (element.type === 'debit') {
            valueLabel.innerText = `Valor: - $ ${element.value.toFixed(2)}`
            if (element.installmentsNumber === 0)
                filteredTotal -= element.value
        } else {
            valueLabel.innerText = `Valor: + $ ${element.value.toFixed(2)}`
            filteredTotal += element.value
        }
        grid.appendChild(valueLabel)

        if (element.installmentsNumber > 0) {
            let installmentsLabel = document.createElement('span')
            installmentsLabel.classList.add('grid-label')
            installmentsLabel.innerText = `N° parcelas: ${element.installmentsNumber}`
            grid.appendChild(installmentsLabel)
        }

        button.appendChild(grid)
        list.appendChild(button)
    }

    let totalParams = new URLSearchParams()
    if (accountId) totalParams.append('accountId', accountId)
    let totalValue = doRequest(`/api/accounts/total-value?${totalParams.toString()}`, 'GET') ?? 0

    document.getElementById('home-total-box').innerHTML = `Saldo em conta(s) <br>$ ${Number(totalValue).toFixed(2)}`
    document.getElementById('filtered-total-box').innerHTML = `Valor baseado em filtro <br>$ ${filteredTotal.toFixed(2)}`
}

function configureFilters() {
    let startDateInput = document.getElementById('start-date-input')
    startDateInput.max = new Date().toISOString().split("T")[0]
    let firstDayOfTheMonth = new Date()
    firstDayOfTheMonth.setDate(1)
    startDateInput.value = firstDayOfTheMonth.toISOString().split("T")[0]

    let endDateInput = document.getElementById('end-date-input')
    endDateInput.max = new Date().toISOString().split("T")[0]
    endDateInput.value = endDateInput.max

    let categoryInput = document.getElementById('category-input')
    let accountInput = document.getElementById('account-input')

    categoryInput.addEventListener('change', updateTransactionList)
    accountInput.addEventListener('change', updateTransactionList)
    startDateInput.addEventListener('change', updateTransactionList)
    endDateInput.addEventListener('change', updateTransactionList)
}

function updateTransactionList() {
    document.getElementById('last-transaction-list').innerHTML = ''
    populateTransactionsList()
}
