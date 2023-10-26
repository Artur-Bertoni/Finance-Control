import {doRequest} from "../../../utils/functions.js";

export class Account {
    constructor(id, name, financialInstitution, contact, description) {
        this.id = id
        this.name = name
        this.financialInstitution = financialInstitution
        this.contact = contact
        this.description = description
    }

    static addAccounts() {
        let accounts = doRequest(
            'http://localhost/finance-control/src/backend/resources/AccountResource.php',
            {findAllByUser: true}
        );

        let accountList = document.getElementById('account-input')

        for (const element of accounts) {
            let account = this.processAccount(element)
            let option = document.createElement('option')
            option.value = account.id
            option.innerText = account.name

            accountList.appendChild(option)
        }
    }

    static processAccount(data) {
        return new Account(
            Number(data.id),
            data.name,
            data.financialInstitution.name,
            data.contact,
            data.description
        );
    }
}