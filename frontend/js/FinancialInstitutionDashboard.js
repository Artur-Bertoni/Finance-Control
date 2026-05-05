import {doRequest, navigate} from "../utils/FrontendFunctions.js"
import {FinancialInstitution} from "./class/FinancialInstitutionClass.js"

let financialInstitutions = []

try {
    let data = doRequest('/api/financial-institutions', 'GET')
    for (const element of (data ?? [])) {
        financialInstitutions.push(FinancialInstitution.processFinancialInstitution(element))
    }
} catch (e) {
    console.log('No financial institutions recovered from DB: ' + e)
}

document.getElementById('dashboard-form').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'postFinancialInstitution') navigate('/pages/FinancialInstitution.html')
})

let list = document.getElementById('financial-institutions-list')

for (const element of financialInstitutions) {
    let button = document.createElement('button')
    button.classList.add('dashboard-item-btn')
    button.classList.add('colorful-button')
    button.type = 'button'
    button.style.backgroundColor = '#4BAE50FF'
    button.addEventListener('click', () => navigate(`/pages/FinancialInstitution.html?id=${element.id}`))

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

    let contactLabel = document.createElement('span')
    contactLabel.classList.add('grid-label')
    contactLabel.innerText = element.contact ? `Contato: ${element.contact}` : 'Contato: Não Informado'
    grid.appendChild(contactLabel)

    button.appendChild(grid)
    list.appendChild(button)
}
