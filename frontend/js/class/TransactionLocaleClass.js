import { populateSelect } from '../../utils/FrontendFunctions.js'

export class TransactionLocale {
    constructor(id, name, address, iconKey) {
        this.id = id
        this.name = name
        this.address = address
        this.iconKey = iconKey ?? null
    }

    static addTransactionLocales(elementId) {
        populateSelect(elementId, '/api/transaction-locales', 'iconKey')
    }

    static processTransactionLocale(data) {
        return new TransactionLocale(
            Number(data.id),
            data.name,
            data.address ?? '',
            data.iconKey ?? null
        )
    }
}
