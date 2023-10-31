import {addDeleteIcon, doRequest} from "../../utils/FrontendFunctions.js";
import {Account} from "./class/AccountClass.js";
import {Category} from "./class/CategoryClass.js";
import {TransactionLocale} from "./class/TransactionLocaleClass.js";
import {Transaction} from "./class/TransactionClass.js";

Category.addCategories();
TransactionLocale.addTransactionLocales();
Account.addAccounts();

let date = document.getElementById('date-input')
date.max = new Date().toISOString().split("T")[0]
date.value = date.max

tryToPopulateWithData();

function tryToPopulateWithData() {
    let response = doRequest(
        'http://localhost/finance-control/src/backend/resources/TransactionResource.php',
        {findById: true})

    if (response !== undefined) {
        addDeleteIcon()

        let transaction = Transaction.processTransaction(response)
        let accountInput = document.getElementById('account-input')
        let categoryInput = document.getElementById('category-input')
        let typeRadioDebit = document.querySelector('input[name="typeRadio"][value="debit"]');
        let typeRadioCredit = document.querySelector('input[name="typeRadio"][value="credit"]');
        let dateInput = document.getElementById('date-input');
        let valueInput = document.getElementById('value-input');
        let installmentsNumberInput = document.getElementById('installments-number-input');
        let transactionLocaleInput = document.getElementById('transaction-locale-input');
        let obsInput = document.getElementById('obs-input');

        if (transaction.type !== undefined) {
            typeRadioDebit.checked = transaction.type === "debit";
            typeRadioCredit.checked = transaction.type === "credit";
        }
        if (transaction.date !== undefined)
            dateInput.value = transaction.date;
        if (transaction.value !== undefined)
            valueInput.value = transaction.value;
        if (transaction.installmentsNumber !== undefined)
            installmentsNumberInput.value = transaction.installmentsNumber;
        if (transaction.obs !== undefined)
            obsInput.value = transaction.obs;

        let accountOptions = accountInput.options;
        for (const element of accountOptions)
            if (element.innerText === transaction.account) {
                element.selected = true;
                break;
            }

        let categoryOptions = categoryInput.options;
        for (const element of categoryOptions)
            if (element.innerText === transaction.category) {
                element.selected = true;
                break;
            }

        let localeOptions = transactionLocaleInput.options;
        for (const element of localeOptions)
            if (element.innerText === transaction.transactionLocale) {
                element.selected = true;
                break;
            }
    }
}