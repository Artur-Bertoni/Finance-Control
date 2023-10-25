import {doRequest} from "../../../utils/functions.js";

export class FinancialInstitution {
    constructor(id, name, address, contact) {
        this.id = id
        this.name = name
        this.address = address
        this.contact = contact
    }

    static addFinancialInstitutions() {
        let financialInstitutions = this.processFinancialInstitution(doRequest(
            'http://localhost/finance-control/src/backend/resources/FinancialInstitutionResource.php',
            {findAllByUser: true}));

        let financialInstitutionList = document.getElementById('financial-institution-input')

        for (const element of financialInstitutions) {
            let option = document.createElement('option')
            option.value = element.id
            option.innerText = element.name

            financialInstitutionList.appendChild(option)
        }
    }

    static processFinancialInstitution(data) {
        let array = [];
        for (const element of data) {
            const financialInstitutionData = element;
            const financialInstitution = new FinancialInstitution(
                Number(financialInstitutionData.id),
                financialInstitutionData.name,
                financialInstitutionData.address,
                financialInstitutionData.contact
            );

            array.push(financialInstitution);
        }

        return array;
    }
}