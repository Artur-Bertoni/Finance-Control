import {Category} from "./class/CategoryClass.js";
import {TransactionLocale} from "./class/TransactionlocaleClass.js";
import {Account} from "./class/AccountClass.js";
import {Transaction} from "./class/TransactionClass.js";

addCategories();
addTransactionLocales();
addAccounts();

let date = document.getElementById('date-input')
date.max = new Date().toISOString().split("T")[0]
date.value = date.max

tryToPopulateWithData();

function tryToPopulateWithData() {
    let deleteImg = document.createElement('img')
    deleteImg.alt = 'Delete Picture'
    deleteImg.id = 'delete-btn-img'
    deleteImg.src = '../images/delete.png'

    let deleteButton = document.createElement('button')
    deleteButton.classList.add('img-btn')
    deleteButton.id = 'delete-btn'
    deleteButton.name = 'deleteButton'
    deleteButton.type = 'submit'

    deleteButton.appendChild(deleteImg)
    let iconButtonList = document.getElementById('navigation-menu-btn-gp')
    iconButtonList.appendChild(deleteButton)

    let response = doRequest(
        'http://localhost/finance-control/src/backend/resources/TransactionResource.php',
        {findById: true})

    console.log(response)

    if (response !== undefined) {
        let transaction = processTransaction(response)
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
        if (transaction.date !== undefined) {
            dateInput.value = transaction.date;
        }
        if (transaction.value !== undefined) {
            valueInput.value = transaction.value;
        }
        if (transaction.installmentsNumber !== undefined) {
            installmentsNumberInput.value = transaction.installmentsNumber;
        }
        if (transaction.obs !== undefined) {
            obsInput.value = transaction.obs;
        }

        let accountOptions = accountInput.options;
        for (const element of accountOptions) {
            if (element.innerText === transaction.account) {
                element.selected = true;
                break;
            }
        }

        let categoryOptions = categoryInput.options;
        for (const element of categoryOptions) {
            if (element.innerText === transaction.category) {
                element.selected = true;
                break;
            }
        }

        let localeOptions = transactionLocaleInput.options;
        for (const element of localeOptions) {
            if (element.innerText === transaction.transactionLocale) {
                element.selected = true;
                break;
            }
        }
    }
}

function addCategories() {
    let categories = processCategories(doRequest(
        'http://localhost/finance-control/src/backend/resources/CategoryResource.php',
        {findAllByUser: true}));

    let categoryList = document.getElementById('category-input')

    for (const element of categories) {
        let option = document.createElement('option')
        option.value = element.id
        option.innerText = element.name

        categoryList.appendChild(option)
    }
}

function addTransactionLocales() {
    let transactionLocales = processTransactionLocales(doRequest(
        'http://localhost/finance-control/src/backend/resources/TransactionLocaleResource.php',
        {findAllByUser: true}));

    let transactionLocaleList = document.getElementById('transaction-locale-input')

    for (const element of transactionLocales) {
        let option = document.createElement('option')
        option.value = element.id
        option.innerText = element.name

        transactionLocaleList.appendChild(option)
    }
}

function addAccounts() {
    let accounts = processAccounts(doRequest(
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

function processCategories(data) {
    let array = [];
    for (const element of data) {
        const categoryData = element;
        const category = new Category(
            Number(categoryData.id),
            categoryData.name,
            categoryData.description
        );

        array.push(category);
    }

    return array;
}

function processTransactionLocales(data) {
    let array = [];
    for (const element of data) {
        const transactionLocaleData = element;
        const transactionLocale = new TransactionLocale(
            Number(transactionLocaleData.id),
            transactionLocaleData.name,
            transactionLocaleData.address
        );

        array.push(transactionLocale);
    }

    return array;
}

function processAccounts(data) {
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

function processTransaction(data) {
    return new Transaction(
        Number(data.id),
        data.account.name,
        data.category.name,
        data.transactionLocale.name,
        Number(data.value),
        data.date,
        data.type,
        Number(data.installmentsNumber),
        data.obs
    );
}

function doRequest(url, method) {
    let data;

    $.ajax({
        url: url,
        type: 'POST',
        async: false,
        data: method,
        success: function (response) {
            data = JSON.parse(response);
        },
        error: function (error) {
            console.error(error);
        }
    });

    return data;
}