import { populateSelect } from '../../utils/FrontendFunctions.js'

export class TransactionLocale {
    constructor(id, name, address) {
        this.id = id
        this.name = name
        this.address = address
    }

    static addTransactionLocales(elementId) {
        populateSelect(elementId, '/api/transaction-locales')
    }

    static processTransactionLocale(data) {
        return new TransactionLocale(
            Number(data.id),
            data.name,
            data.address ?? ''
        )
    }
}
