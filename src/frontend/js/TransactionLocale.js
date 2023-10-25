import {addDeleteIcon, doRequest} from "../../utils/functions.js";
import {TransactionLocale} from "./class/TransactionLocaleClass.js";

tryToPopulateWithData();

function tryToPopulateWithData() {
    let response = doRequest(
        'http://localhost/finance-control/src/backend/resources/TransactionLocaleResource.php',
        {findById: true})

    addDeleteIcon()

    if (response !== undefined) {
        let transactionLocale = TransactionLocale.processTransactionLocale(response)
        let nameInput = document.getElementById('name-input')
        let addressInput = document.getElementById('address-input')

        if (transactionLocale.name !== undefined)
            nameInput.value = transactionLocale.name
        if (transactionLocale.address !== undefined)
            addressInput.value = transactionLocale.address
    }
}