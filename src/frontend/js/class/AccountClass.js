import {doRequest} from "../../../utils/functions";

export class Account {
    constructor(id, name, financialInstitution, contact, description) {
        this.id = id
        this.name = name
        this.financialInstitution = financialInstitution
        this.contact = contact
        this.description = description
    }

    static addAccounts() {
        let accounts = this.processAccounts(doRequest(
            'http://localhost/finance-control/src/backend/resources/AccountResource.php',
            {findAllByUser: true}));

        let accountList = document.getElementById('account-input')

        for (const element of accounts) {
            let option = document.createElement('option')
            option.value = element.id
            option.innerText = element.name

            accountList.appendChild(option)
        }
    }

    static processAccounts(data) {
        let array = [];
        for (const element of data) {
            const accountData = element;
            const account = new Account(
                Number(accountData.id),
                accountData.name,
                accountData.financialInstitution.name,
                accountData.contact,
                accountData.description
            );

            array.push(account);
        }

        return array;
    }
}