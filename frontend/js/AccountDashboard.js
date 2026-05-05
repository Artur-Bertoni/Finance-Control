import {doRequest, navigate} from "../utils/FrontendFunctions.js"
import {Account} from "./class/AccountClass.js"

let accounts = []

try {
    let data = doRequest('/api/accounts', 'GET')
    for (const element of (data ?? [])) {
        accounts.push(Account.processAccount(element))
    }
} catch (e) {
    console.log('No accounts recovered from DB: ' + e)
}

document.getElementById('dashboard-form').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'postAccount') navigate('/pages/Account.html')
    else if (name === 'postTransfer') navigate('/pages/Transfer.html')
})

let list = document.getElementById('accounts-list')

for (const element of accounts) {
    let button = document.createElement('button')
    button.classList.add('dashboard-item-btn')
    button.classList.add('colorful-button')
    button.type = 'button'
    button.style.backgroundColor = '#4BAE50FF'
    button.addEventListener('click', () => navigate(`/pages/Account.html?id=${element.id}`))

    let grid = document.createElement('div')
    grid.classList.add('dashboard-grid')

    let nameLabel = document.createElement('span')
    nameLabel.classList.add('grid-label')
    nameLabel.innerText = `Nome: ${element.name}`
    grid.appendChild(nameLabel)

    let financialInstitutionLabel = document.createElement('span')
    financialInstitutionLabel.classList.add('grid-label')
    financialInstitutionLabel.innerText = `Instituição Financeira: ${element.financialInstitution}`
    grid.appendChild(financialInstitutionLabel)

    let contactLabel = document.createElement('span')
    contactLabel.classList.add('grid-label')
    contactLabel.innerText = element.contact ? `Contato: ${element.contact}` : 'Contato: Não Informado'
    grid.appendChild(contactLabel)

    let balanceLabel = document.createElement('span')
    balanceLabel.classList.add('grid-label')
    if (element.balance < 0)
        balanceLabel.innerText = `Saldo: - $ ${Math.abs(element.balance).toFixed(2)}`
    else if (element.balance > 0)
        balanceLabel.innerText = `Saldo: + $ ${element.balance.toFixed(2)}`
    else
        balanceLabel.innerText = `Saldo: $ ${element.balance.toFixed(2)}`
    grid.appendChild(balanceLabel)

    button.appendChild(grid)
    list.appendChild(button)
}
