import {FinancialInstitution} from "./class/FinancialInstitutionClass.js";
import {addDeleteIcon, doRequest} from "../../utils/FrontendFunctions.js";
import {Account} from "./class/AccountClass.js";

FinancialInstitution.addFinancialInstitutions();

tryToPopulateWithData();

function tryToPopulateWithData() {
    let response = doRequest(
        'http://localhost/finance-control/src/backend/resources/AccountResource.php',
        {findById: true})

    addDeleteIcon()

    if (response !== undefined) {
        let account = Account.processAccount(response);
        let nameInput = document.getElementById('name-input')
        let financialInstitutionInput = document.getElementById('financial-institution-input')
        let contactInput = document.getElementById('contact-input')
        let descriptionInput = document.getElementById('description-input')

        if (account.name !== undefined)
            nameInput.value = account.name
        if (account.contact !== undefined)
            contactInput.value = account.contact
        if (account.description !== undefined)
            descriptionInput.value = account.description

        let financialInstitutionOptions = financialInstitutionInput.options;
        for (const element of financialInstitutionOptions)
            if (element.innerText === account.financialInstitution) {
                element.selected = true;
                break;
            }
    }
}
