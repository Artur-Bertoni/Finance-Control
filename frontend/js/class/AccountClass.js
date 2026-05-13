import { populateSelect } from '../../utils/FrontendFunctions.js'

export class Account {
    constructor(id, name, financialInstitution, contact, description, balance, financialInstitutionId) {
        this.id = id
        this.name = name
        this.financialInstitution = financialInstitution
        this.contact = contact
        this.description = description
        this.balance = balance
        this.financialInstitutionId = financialInstitutionId
    }

    static addAccounts(elementId) {
        populateSelect(elementId, '/api/accounts')
    }

    static processAccount(data) {
        return new Account(
            Number(data.id),
            data.name,
            data.financialInstitution?.name ?? '',
            data.contact ?? '',
            data.description ?? '',
            Number(data.balance),
            data.financialInstitution?.id ?? null
        )
    }
}
