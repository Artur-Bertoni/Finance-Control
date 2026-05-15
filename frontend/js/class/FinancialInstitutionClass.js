import { populateSelect } from '../../utils/FrontendFunctions.js'

export class FinancialInstitution {
    constructor(id, name, address, contact, iconKey) {
        this.id = id
        this.name = name
        this.address = address
        this.contact = contact
        this.iconKey = iconKey ?? null
    }

    static addFinancialInstitutions(elementId = 'financial-institution-input') {
        populateSelect(elementId, '/api/financial-institutions', 'iconKey')
    }

    static processFinancialInstitution(data) {
        return new FinancialInstitution(
            Number(data.id),
            data.name,
            data.address ?? '',
            data.contact ?? '',
            data.iconKey ?? null
        )
    }
}
