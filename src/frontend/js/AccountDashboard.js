import {doRequest} from "../../utils/FrontendFunctions.js";
import {Account} from "./class/AccountClass.js";

let data;
let accounts = []

data = doRequest('http://localhost/finance-control/src/backend/resources/AccountResource.php',
    {findAllByUser: true})

try {
    for (const element of data) {
        const account = Account.processAccount(element)
        accounts.push(account)
    }
} catch (e) {
    console.log('No accounts recovered from DB: ' + e)
}

let list = document.getElementById('accounts-list')

for (const element of accounts) {
    let button = document.createElement('button')
    button.classList.add('dashboard-item-btn')
    button.classList.add('colorful-button')
    button.type = "submit"
    button.name = "itemButton"
    button.value = element.id
    button.style.backgroundColor = '#4BAE50FF'

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
    if (element.contact != "")
        contactLabel.innerText = `Contato: ${element.contact}`
    else contactLabel.innerText = `Contato: Não Informado`
    grid.appendChild(contactLabel)

    let balanceLabel = document.createElement('span')
    balanceLabel.classList.add('grid-label')

    if (element.balance < 0)
        balanceLabel.innerText = `Saldo: - $ ${element.balance.toFixed(2)}`
    else if (element.balance > 0)
        balanceLabel.innerText = `Saldo: + $ ${element.balance.toFixed(2)}`
    else
        balanceLabel.innerText = `Saldo: $ ${element.balance.toFixed(2)}`
    grid.appendChild(balanceLabel)

    button.appendChild(grid)
    list.appendChild(button)
}