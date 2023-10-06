class Transaction {
    constructor(category, account, type, transactionLocale, date, value, isInstallments, installmentsNumber, lastCharge) {
        this.category = category
        this.account = account
        this.type = type
        this.transactionLocale = transactionLocale
        this.date = date
        this.value = value
        this.intallmentsNumber = installmentsNumber
    }
}

let data =

    [ new Transaction('Categoria exemplo', 'Conta Corrente', 'D', 'Local de Gasto', "01/01/2023", 100),
        new Transaction('Categoria exemplo', 'Conta Corrente', 'D', 'Local de Gasto', "01/01/2023", 100),
        new Transaction('Categoria exemplo', 'Conta Corrente', 'C', 'Local de Gasto', "01/01/2023", 100),
        new Transaction('Categoria exemplo', 'Conta Corrente', 'D', 'Local de Gasto', "01/01/2023", 100),
        new Transaction('Categoria exemplo', 'Cartão Crédito', 'D', 'Local de Gasto', "01/01/2023", 100, 3),
        new Transaction('Categoria exemplo', 'Conta Corrente', 'D', 'Local de Gasto', "01/01/2023", 100),
        new Transaction('Categoria exemplo', 'Conta Corrente', 'D', 'Local de Gasto', "01/01/2023", 100)
    ]

let list = document.getElementById('last-transaction-list')
let totalValue = 0

for (let i = 0; i < 30; i++) {
    let button = document.createElement('button')
    button.classList.add('transaction-item-btn')
    button.classList.add('colorful-button')
    button.type = "submit"
    button.name = "itemButton"

    if (data[i].type === 'D' && data[i].installmentsNumber === 0)
        button.style.backgroundColor = '#C04C4CCC'
    else if (data[i].type === 'D' && data[i].installmentsNumber > 0)
        button.style.backgroundColor = '#0085b6'
    else
        button.style.backgroundColor = '#4BAE50FF'

    let grid = document.createElement('div')
    grid.classList.add('transaction-grid')

    let categoryLabel = document.createElement('span')
    categoryLabel.classList.add('grid-label')
    categoryLabel.innerText = `${data[i].category}`
    grid.appendChild(categoryLabel)

    let accountLabel = document.createElement('span')
    accountLabel.classList.add('grid-label')
    accountLabel.innerText = `${data[i].account}`
    grid.appendChild(accountLabel)

    let localeLabel = document.createElement('span')
    localeLabel.classList.add('grid-label')
    localeLabel.innerText = `${data[i].transactionLocale}`
    grid.appendChild(localeLabel)

    let dateLabel = document.createElement('span')
    dateLabel.classList.add('grid-label')
    dateLabel.innerText = `Data: ${data[i].date}`
    grid.appendChild(dateLabel)

    let valueLabel = document.createElement('span')
    valueLabel.classList.add('grid-label')

    if (data[i].type === 'D') {
        valueLabel.innerText = `- $ ${data[i].value.toFixed(2)}`

        if (data[i].isInstallments === false)
            totalValue -= data[i].value
    } else {
        valueLabel.innerText = `+ $ ${data[i].value.toFixed(2)}`
        totalValue += data[i].value
    }

    grid.appendChild(valueLabel)

    if (data[i].installmentsNumber > 0) {
        let installmentsNumber = document.createElement('span')
        installmentsNumber.classList.add('grid-label')
        installmentsNumber.innerText = `N° parcelas: ${data[i].intallmentsNumber}`
        grid.appendChild(installmentsNumber)
    }

    button.appendChild(grid)
    list.appendChild(button)

    document.getElementById('home-total-box').innerHTML = `Saldo total em conta <br>$ ${totalValue.toFixed(2)}`
}