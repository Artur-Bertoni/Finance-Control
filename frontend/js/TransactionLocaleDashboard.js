import {doRequest, navigate} from "../utils/FrontendFunctions.js"
import {TransactionLocale} from "./class/TransactionLocaleClass.js"

let transactionLocales = []

try {
    let data = doRequest('/api/transaction-locales', 'GET')
    for (const element of (data ?? [])) {
        transactionLocales.push(TransactionLocale.processTransactionLocale(element))
    }
} catch (e) {
    console.log('No transaction locales recovered from DB: ' + e)
}

document.getElementById('dashboard-form').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'postTransactionLocale') navigate('/pages/TransactionLocale.html')
})

let list = document.getElementById('transaction-locales-list')

for (const element of transactionLocales) {
    let button = document.createElement('button')
    button.classList.add('dashboard-item-btn')
    button.classList.add('colorful-button')
    button.type = 'button'
    button.style.backgroundColor = '#4BAE50FF'
    button.addEventListener('click', () => navigate(`/pages/TransactionLocale.html?id=${element.id}`))

    let grid = document.createElement('div')
    grid.classList.add('dashboard-grid')

    let nameLabel = document.createElement('span')
    nameLabel.classList.add('grid-label')
    nameLabel.innerText = `Nome: ${element.name}`
    grid.appendChild(nameLabel)

    let addressLabel = document.createElement('span')
    addressLabel.classList.add('grid-label')
    addressLabel.innerText = element.address ? `Endereço: ${element.address}` : 'Endereço: Não Informado'
    grid.appendChild(addressLabel)

    button.appendChild(grid)
    list.appendChild(button)
}
