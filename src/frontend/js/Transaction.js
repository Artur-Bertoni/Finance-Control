
let categoryData = [[1, 'Cinema'], [2, 'Alimentação']]
let categoryList = document.getElementById('category-input')

for (const element of categoryData) {
    let option = document.createElement('option')
    option.value = element[0]
    option.innerText = element[1]

    categoryList.appendChild(option)
}

let transactionLocaleData = [[1, 'Local 1'], [2, 'Local 2']]
let transactionLocaleList = document.getElementById('transaction-locale-input')

for (const element of transactionLocaleData) {
    let option = document.createElement('option')
    option.value = element[0]
    option.innerText = element[1]

    transactionLocaleList.appendChild(option)
}

let accountData = [[1, 'Conta Corrente'], [2, 'Cartão Crédito']]
let accountList = document.getElementById('account-input')

for (const element of accountData) {
    let option = document.createElement('option')
    option.value = element[0]
    option.innerText = element[1]

    accountList.appendChild(option)
}

let date = document.getElementById('date-input')
date.max = new Date().toISOString().split("T")[0]
date.value = date.max