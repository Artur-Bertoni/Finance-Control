import {doRequest} from "../../../utils/functions.js";

export class TransactionLocale {
    constructor(id, name, address) {
        this.id = id
        this.name = name
        this.address = address
    }

    static addTransactionLocales() {
        let transactionLocales = this.processTransactionLocales(doRequest(
            'http://localhost/finance-control/src/backend/resources/TransactionLocaleResource.php',
            {findAllByUser: true}));

        let transactionLocaleList = document.getElementById('transaction-locale-input')

        for (const element of transactionLocales) {
            let option = document.createElement('option')
            option.value = element.id
            option.innerText = element.name

            transactionLocaleList.appendChild(option)
        }
    }

    static processTransactionLocales(data) {
        let array = [];
        for (const element of data) {
            const transactionLocaleData = element;
            const transactionLocale = new TransactionLocale(
                Number(transactionLocaleData.id),
                transactionLocaleData.name,
                transactionLocaleData.address
            );

            array.push(transactionLocale);
        }

        return array;
    }
}