import {doRequest} from "../../utils/functions.js";
import {TransactionLocale} from "./class/TransactionLocaleClass.js";

let data;
let transactionLocales = []

data = doRequest('http://localhost/finance-control/src/backend/resources/TransactionLocaleResource.php',
    {findAllByUser: true})

try {
    for (const element of data) {
        const transactionLocale = TransactionLocale.processTransactionLocale(element)
        transactionLocales.push(transactionLocale)
    }
} catch(e) {
    console.log('No transaction locales recovered from DB: ' + e)
}

let list = document.getElementById('transaction-locales-list')

for (const element of transactionLocales) {
    let button = document.createElement('button')
    button.classList.add('dashboard-item-btn')
    button.classList.add('colorful-button')
    button.type = "submit"
    button.name = "itemButton"
    button.value = element.id
    button.style.backgroundColor = '#4BAE50FF'

    let grid = document.createElement('div')
    grid.classList.add('transaction-locale-grid')

    let nameLabel = document.createElement('span')
    nameLabel.classList.add('grid-label')
    nameLabel.innerText = `Nome: ${element.name}`
    grid.appendChild(nameLabel)

    let addressLabel = document.createElement('span')
    addressLabel.classList.add('grid-label')
    addressLabel.innerText = `Endereço: ${element.address}`
    grid.appendChild(addressLabel)

    button.appendChild(grid)
    list.appendChild(button)
}