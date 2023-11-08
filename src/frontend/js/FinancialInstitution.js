import {addDeleteIcon, doRequest} from "../../utils/FrontendFunctions.js";
import {FinancialInstitution} from "./class/FinancialInstitutionClass.js";

tryToPopulateWithData();

function tryToPopulateWithData() {
    let response = doRequest(
        'http://localhost/finance-control/src/backend/resources/FinancialInstitutionResource.php',
        {findById: true})

    let financialInstitution = FinancialInstitution.processFinancialInstitution(response)
    let nameInput = document.getElementById('name-input')
    let addressInput = document.getElementById('address-input')
    let contactInput = document.getElementById('contact-input')

    if (financialInstitution.name !== undefined)
        nameInput.value = financialInstitution.name
    if (financialInstitution.address !== undefined)
        addressInput.value = financialInstitution.address
    if (financialInstitution.contact !== undefined)
        contactInput.value = financialInstitution.contact

    addDeleteIcon()
}