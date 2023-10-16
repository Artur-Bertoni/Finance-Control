import {Category} from "./class/CategoryClass.js";
import {TransactionLocale} from "./class/TransactionlocaleClass.js";
import {Account} from "./class/AccountClass.js";

addCategories();
addTransactionLocales();
addAccounts();

let date = document.getElementById('date-input')
date.max = new Date().toISOString().split("T")[0]
date.value = date.max

function addCategories() {
    let categories = processCategory(doRequest(
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
    let transactionLocales = processCategory(doRequest(
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
    let accounts = processCategory(doRequest(
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

function processCategory(data) {
    let array = [];
    for (const element of data) {
        const categoryData = element;
        const category = new Category(
            Number(categoryData.id),
            categoryData.name,
            categoryData.description
        );

        array.push(category);
        console.log(category)
    }

    return array;
}

function processTransactionLocale(data) {
    let array = [];
    for (const element of data) {
        const transactionLocaleData = element;
        const transactionLocale = new TransactionLocale(
            Number(transactionLocaleData.id),
            transactionLocaleData.name,
            transactionLocaleData.address
        );

        array.push(transactionLocale);
        console.log(transactionLocale)
    }

    return array;
}

function processAccount(data) {
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
        console.log(account)
    }

    return array;
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