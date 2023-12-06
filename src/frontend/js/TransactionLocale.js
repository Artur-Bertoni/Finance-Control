import {addDeleteIcon, doRequest} from "../../utils/FrontendFunctions.js";
import {TransactionLocale} from "./class/TransactionLocaleClass.js";

tryToPopulateWithData();

document.getElementById('save-btn').addEventListener("click", function () {
    let nameInput = document.getElementById('name-input').value

    if (nameInput === '')
        alert('O campo Nome deve ser preenchido!');
    else
        document.form.submit();
});

function tryToPopulateWithData() {
    let response = doRequest(
        'http://localhost/finance-control/src/backend/resources/TransactionLocaleResource.php',
        {findById: true})

    if (response) {
        let transactionLocale = TransactionLocale.processTransactionLocale(response)
        let nameInput = document.getElementById('name-input')
        let addressInput = document.getElementById('address-input')

        if (transactionLocale.name !== undefined)
            nameInput.value = transactionLocale.name
        if (transactionLocale.address !== undefined)
            addressInput.value = transactionLocale.address

        addDeleteIcon()
    }
}