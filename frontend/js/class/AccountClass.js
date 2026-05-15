import { populateSelect } from '../../utils/FrontendFunctions.js'

export class Account {
    constructor({ id, name, financialInstitution, contact, description, balance, financialInstitutionId, iconKey }) {
        this.id = id
        this.name = name
        this.financialInstitution = financialInstitution
        this.contact = contact
        this.description = description
        this.balance = balance
        this.financialInstitutionId = financialInstitutionId
        this.iconKey = iconKey ?? null
    }

    static addAccounts(elementId) {
        populateSelect(elementId, '/api/accounts', 'iconKey')
    }

    static processAccount(data) {
        return new Account({
            id:                   Number(data.id),
            name:                 data.name,
            financialInstitution: data.financialInstitution?.name ?? '',
            contact:              data.contact ?? '',
            description:          data.description ?? '',
            balance:              Number(data.balance),
            financialInstitutionId: data.financialInstitution?.id ?? null,
            iconKey:              data.iconKey ?? null,
        })
    }
}
