import {FinancialInstitution} from "./class/FinancialInstitutionClass.js";
import {addDeleteIcon, doRequest} from "../../utils/FrontendFunctions.js";
import {Account} from "./class/AccountClass.js";

FinancialInstitution.addFinancialInstitutions();

tryToPopulateWithData();

document.getElementById('save-btn').addEventListener("click", function () {
    let nameInput = document.getElementById('name-input').value
    let financialInstitutionInput = document.getElementById('financial-institution-input').value
    let balanceInput = document.getElementById('balance-input').value

    if (nameInput === '') {
        alert('O campo Nome deve ser preenchido!')
    } else if (financialInstitutionInput === '') {
        alert('O campo Instituição Financeira deve ser preenchido!')
    } else if (balanceInput === '') {
        alert('O campo Saldo deve ser preenchido!')
    } else {
        document.form.submit();
    }

    //TODO implementar onclick em todos os botões do sistema
});

function tryToPopulateWithData() {
    let response = doRequest(
        'http://localhost/finance-control/src/backend/resources/AccountResource.php',
        {findById: true})

    if (!response) {
        let account = Account.processAccount(response);
        let nameInput = document.getElementById('name-input')
        let financialInstitutionInput = document.getElementById('financial-institution-input')
        let contactInput = document.getElementById('contact-input')
        let descriptionInput = document.getElementById('description-input')
        let balanceInput = document.getElementById('balance-input')

        if (account.name !== undefined)
            nameInput.value = account.name
        if (account.contact !== undefined)
            contactInput.value = account.contact
        if (account.description !== undefined)
            descriptionInput.value = account.description
        if (account.balance !== undefined)
            balanceInput.value = account.balance

        let financialInstitutionOptions = financialInstitutionInput.options;
        for (const element of financialInstitutionOptions)
            if (element.innerText === account.financialInstitution) {
                element.selected = true;
                break;
            }

        addDeleteIcon()
    }
}
