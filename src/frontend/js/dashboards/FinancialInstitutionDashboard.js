import {doRequest} from "../../../utils/functions.js";
import {FinancialInstitution} from "../class/FinancialInstitutionClass.js";

let data;
let financialInstitutions = []

data = doRequest('http://localhost/finance-control/src/backend/resources/FinancialInstitutionResource.php',
    {findAllByUser: true})

try {
    for (const element of data) {
        const financialInstitution = FinancialInstitution.processFinancialInstitution(element)
        financialInstitutions.push(financialInstitution)
    }
} catch(e) {
    console.log('No financial institutions recovered from DB: ' + e)
}

let list = document.getElementById('financial-institutions-list')

for (const element of financialInstitutions) {
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

    let addressLabel = document.createElement('span')
    addressLabel.classList.add('grid-label')
    addressLabel.innerText = `Endereço: ${element.address}`
    grid.appendChild(addressLabel)

    let contactLabel = document.createElement('span')
    contactLabel.classList.add('grid-label')
    contactLabel.innerText = `Contato: ${element.contact}`
    grid.appendChild(contactLabel)

    button.appendChild(grid)
    list.appendChild(button)
}