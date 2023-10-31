import {doRequest} from "../../../utils/FrontendFunctions.js";

export class TransactionLocale {
    constructor(id, name, address) {
        this.id = id
        this.name = name
        this.address = address
    }

    static addTransactionLocales() {
        let transactionLocales = doRequest(
            'http://localhost/finance-control/src/backend/resources/TransactionLocaleResource.php',
            {findAllByUser: true}
        );

        let transactionLocaleList = document.getElementById('transaction-locale-input')

        for (const element of transactionLocales) {
            let transactionLocale = this.processTransactionLocale(element)
            let option = document.createElement('option')
            option.value = transactionLocale.id
            option.innerText = transactionLocale.name

            transactionLocaleList.appendChild(option)
        }
    }

    static processTransactionLocale(data) {
        return new TransactionLocale(
            Number(data.id),
            data.name,
            data.address
        );
    }
}