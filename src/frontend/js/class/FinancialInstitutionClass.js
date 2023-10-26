import {doRequest} from "../../../utils/functions.js";

export class FinancialInstitution {
    constructor(id, name, address, contact) {
        this.id = id
        this.name = name
        this.address = address
        this.contact = contact
    }

    static addFinancialInstitutions() {
        let financialInstitutions = doRequest(
            'http://localhost/finance-control/src/backend/resources/FinancialInstitutionResource.php',
            {findAllByUser: true}
        );

        let financialInstitutionList = document.getElementById('financial-institution-input')

        for (const element of financialInstitutions) {
            let financialInstitution = this.processFinancialInstitution(element)
            let option = document.createElement('option')
            option.value = financialInstitution.id
            option.innerText = financialInstitution.name

            financialInstitutionList.appendChild(option)
        }
    }

    static processFinancialInstitution(data) {
        return new FinancialInstitution(
            Number(data.id),
            data.name,
            data.address,
            data.contact
        );
    }
}