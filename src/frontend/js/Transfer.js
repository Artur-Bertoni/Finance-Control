import {Account} from "./class/AccountClass.js";
import {Category} from "./class/CategoryClass.js";
import {TransactionLocale} from "./class/TransactionLocaleClass.js";
import {doRequest} from "../../utils/FrontendFunctions.js";

Category.addCategories('category-input');
TransactionLocale.addTransactionLocales('transfer-locale-input');
Account.addAccounts('origin-account-input');
Account.addAccounts('destination-account-input');

let date = document.getElementById('date-input')
date.max = new Date().toISOString().split("T")[0]
date.value = date.max

document.getElementById('origin-account-input').addEventListener('change', setMaxValue)

document.getElementById('save-btn').addEventListener("click", function () {
    let originAccountInput = document.getElementById('origin-account-input').value
    let DestinationAccountInput = document.getElementById('destination-account-input').value
    let categoryInput = document.getElementById('category-input').value
    let valueInput = document.getElementById('value-input').value
    let dateInput = document.getElementById('date-input').value

    if (originAccountInput === '' || DestinationAccountInput === '' || categoryInput === '' || dateInput === '' || valueInput === '')
        alert('Os campos Conta de Origem, Conta Destino, Categoria, Valor e Data devem ser preenchidos!');
    else
        document.form.submit();
});

function setMaxValue() {
    let valueInput = document.getElementById('value-input')

    valueInput.max = doRequest('http://localhost/finance-control/src/backend/resources/AccountResource.php',
            {totalAccountsValue: true},
            {accountId: document.getElementById('origin-account-input').value}
        );

    valueInput.placeholder = 'Valor ($) - Max: $ ' + Number(valueInput.max).toFixed(2)
    valueInput.width = '1000px'
}
