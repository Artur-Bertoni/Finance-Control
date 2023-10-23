import {Transaction} from "./class/TransactionClass.js";

let data;
let transactions = [];

$.ajax({
    url: 'http://localhost/finance-control/src/backend/resources/TransactionResource.php',
    type: 'POST',
    async: false,
    data: {findAllByUser: true},
    success: function (response) {
        data = JSON.parse(response);
        transactions = processData(data);
    },
    error: function (error) {
        console.error(error);
    }
});

function processData(data) {
    let array = [];
    for (const element of data) {
        const transactionData = element;
        const transaction = new Transaction(
            Number(transactionData.id),
            transactionData.account.name,
            transactionData.category.name,
            transactionData.transactionLocale.name,
            Number(transactionData.value),
            transactionData.date,
            transactionData.type,
            Number(transactionData.installmentsNumber)
        );

        array.push(transaction);
    }

    return array;
}

let list = document.getElementById('last-transaction-list')
let totalValue = 0

for (const element of transactions) {
    let button = document.createElement('button')
    button.classList.add('transaction-item-btn')
    button.classList.add('colorful-button')
    button.type = "submit"
    button.name = "itemButton"
    button.value = element.id

    if (element.type === 'debit' && element.installmentsNumber === 0)
        button.style.backgroundColor = '#C04C4CCC'
    else if (element.type === 'debit' && element.installmentsNumber > 0)
        button.style.backgroundColor = '#0085b6'
    else
        button.style.backgroundColor = '#4BAE50FF'

    let grid = document.createElement('div')
    grid.classList.add('transaction-grid')

    let categoryLabel = document.createElement('span')
    categoryLabel.classList.add('grid-label')
    categoryLabel.innerText = `Categoria: ${element.category}`
    grid.appendChild(categoryLabel)

    let accountLabel = document.createElement('span')
    accountLabel.classList.add('grid-label')
    accountLabel.innerText = `Conta: ${element.account}`
    grid.appendChild(accountLabel)

    let localeLabel = document.createElement('span')
    localeLabel.classList.add('grid-label')
    localeLabel.innerText = `Local: ${element.transactionLocale}`
    grid.appendChild(localeLabel)

    let dateLabel = document.createElement('span')
    dateLabel.classList.add('grid-label')
    dateLabel.innerText = `Data: ${element.date}`
    grid.appendChild(dateLabel)

    let valueLabel = document.createElement('span')
    valueLabel.classList.add('grid-label')

    if (element.type === 'debit') {
        valueLabel.innerText = `Valor: - $ ${element.value.toFixed(2)}`

        if (element.installmentsNumber === 0)
            totalValue -= element.value
    } else {
        valueLabel.innerText = `Valor: + $ ${element.value.toFixed(2)}`
        totalValue += element.value
    }

    grid.appendChild(valueLabel)

    if (element.installmentsNumber > 0) {
        let installmentsNumber = document.createElement('span')
        installmentsNumber.classList.add('grid-label')
        installmentsNumber.innerText = `N° parcelas: ${element.installmentsNumber}`
        grid.appendChild(installmentsNumber)
    }

    button.appendChild(grid)
    list.appendChild(button)

    document.getElementById('home-total-box').innerHTML = `Saldo total em conta <br>$ ${totalValue.toFixed(2)}`
}