import {doRequest} from "../../utils/functions.js";
import {Transaction} from "./class/TransactionClass.js";

let data;
let transactions = [];

data = doRequest('http://localhost/finance-control/src/backend/resources/TransactionResource.php',
    {findAllByUser: true})

try {
    for (const element of data) {
        const transaction = Transaction.processTransaction(element)
        transactions.push(transaction)
    }
} catch (e) {
    console.log('No transactions recovered from DB: ' + e)
}

let list = document.getElementById('last-transaction-list')
let totalValue = 0

for (const element of transactions) {
    let button = document.createElement('button')
    button.classList.add('dashboard-item-btn')
    button.classList.add('colorful-button')
    button.type = "submit"
    button.name = "itemButton"
    button.value = element.id

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
    localeLabel.innerText = `Local: ${element.transactionLocale}`
    grid.appendChild(localeLabel)

    let dateLabel = document.createElement('span')
    dateLabel.classList.add('grid-label')
    dateLabel.innerText = `Data: ${element.date}`
    grid.appendChild(dateLabel)

    let valueLabel = document.createElement('span')
    valueLabel.classList.add('grid-label')

    if (element.type === 'debit') {
        valueLabel.innerText = `Valor: - $ ${element.value.toFixed(2)}`

        if (element.installmentsNumber === 0)
            totalValue -= element.value
    } else {
        valueLabel.innerText = `Valor: + $ ${element.value.toFixed(2)}`
        totalValue += element.value
    }

    grid.appendChild(valueLabel)

    if (element.installmentsNumber > 0) {
        let installmentsNumber = document.createElement('span')
        installmentsNumber.classList.add('grid-label')
        installmentsNumber.innerText = `N° parcelas: ${element.installmentsNumber}`
        grid.appendChild(installmentsNumber)
    }

    button.appendChild(grid)
    list.appendChild(button)

}

document.getElementById('home-total-box').innerHTML = `Saldo total em conta <br>$ ${totalValue.toFixed(2)}`
