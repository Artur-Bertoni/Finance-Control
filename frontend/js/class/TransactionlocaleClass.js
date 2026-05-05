import {doRequest} from "../../utils/FrontendFunctions.js"

export class TransactionLocale {
    constructor(id, name, address) {
        this.id = id
        this.name = name
        this.address = address
    }

    static addTransactionLocales(elementId) {
        let transactionLocales = doRequest('/api/transaction-locales', 'GET') ?? []

        let transactionLocaleList = document.getElementById(elementId)
        for (const element of transactionLocales) {
            let locale = this.processTransactionLocale(element)
            let option = document.createElement('option')
            option.value = locale.id
            option.innerText = locale.name
            transactionLocaleList.appendChild(option)
        }
    }

    static processTransactionLocale(data) {
        return new TransactionLocale(
            Number(data.id),
            data.name,
            data.address ?? ''
        )
    }
}
