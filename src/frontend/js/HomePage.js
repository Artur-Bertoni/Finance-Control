class Transaction {
    constructor(account, category, transactionLocale, value, date, type, installmentsNumber) {
        this.account = account
        this.category = category
        this.transactionLocale = transactionLocale
        this.value = value
        this.date = date
        this.type = type
        this.installmentsNumber = installmentsNumber
    }
}

let data;
let transactions = [];

jQuery.ajax({
    url: '../../backend/services/TransactionService.php',
    method: 'POST',
    data: {action: 'findAllByUser', arguments: [12]},
    success: function (response) {
        data = JSON.parse(response).result;
        processData(data);
    },
    error: function (error) {
        //TODo descobrir origem do erro (404) e concertar
        console.log('O código JavaScript está sendo executado.');
        console.error(error);
    }
});

function processData(data) {
    for (const element of data) {
        const transactionData = element;
        const transaction = new Transaction(
            transactionData.category,
            transactionData.account,
            transactionData.type,
            transactionData.transactionLocale,
            transactionData.date,
            transactionData.value,
            transactionData.installmentsNumber
        );

        transactions.push(transaction);
    }
}

let list = document.getElementById('last-transaction-list')
let totalValue = 0

for (const element of transactions) {
    let button = document.createElement('button')
    button.classList.add('transaction-item-btn')
    button.classList.add('colorful-button')
    button.type = "submit"
    button.name = "itemButton"

    if (element.type === 'D' && element.installmentsNumber === 0)
        button.style.backgroundColor = '#C04C4CCC'
    else if (element.type === 'D' && element.installmentsNumber > 0)
        button.style.backgroundColor = '#0085b6'
    else
        button.style.backgroundColor = '#4BAE50FF'

    let grid = document.createElement('div')
    grid.classList.add('transaction-grid')

    let categoryLabel = document.createElement('span')
    categoryLabel.classList.add('grid-label')
    categoryLabel.innerText = `${element.category}`
    grid.appendChild(categoryLabel)

    let accountLabel = document.createElement('span')
    accountLabel.classList.add('grid-label')
    accountLabel.innerText = `${element.account}`
    grid.appendChild(accountLabel)

    let localeLabel = document.createElement('span')
    localeLabel.classList.add('grid-label')
    localeLabel.innerText = `${element.transactionLocale}`
    grid.appendChild(localeLabel)

    let dateLabel = document.createElement('span')
    dateLabel.classList.add('grid-label')
    dateLabel.innerText = `Data: ${element.date}`
    grid.appendChild(dateLabel)

    let valueLabel = document.createElement('span')
    valueLabel.classList.add('grid-label')

    if (element.type === 'D') {
        valueLabel.innerText = `- $ ${element.value.toFixed(2)}`

        if (element.installmentsNumber === 0)
            totalValue -= element.value
    } else {
        valueLabel.innerText = `+ $ ${element.value.toFixed(2)}`
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