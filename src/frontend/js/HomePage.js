import {doRequest} from "../../utils/FrontendFunctions.js";
import {Transaction} from "./class/TransactionClass.js";

setDefaultDate()
populateTransactionsList()

function populateTransactionsList() {
    let data;
    let transactions = [];

    data = doRequest('http://localhost/finance-control/src/backend/resources/TransactionResource.php',
        {findAllByUser: true},
        {
            startDate: document.getElementById('start-date-input').value,
            endDate: document.getElementById('end-date-input').value
        })

    console.log(data)

    try {
        for (const element of data) {
            const transaction = Transaction.processTransaction(element)
            transactions.push(transaction)
        }
    } catch (e) {
        console.log('No transactions recovered from DB: ' + e)
    }

    let list = document.getElementById('last-transaction-list')
    let totalValue = 0

    for (const element of transactions) {
        let button = document.createElement('button')
        button.classList.add('dashboard-item-btn')
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
        grid.classList.add('dashboard-grid')

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
        if (element.transactionLocale !== undefined)
            localeLabel.innerText = `Local: ${element.transactionLocale}`
        else
            localeLabel.innerText = `Local: Não Informado`
        grid.appendChild(localeLabel)

        let dateLabel = document.createElement('span')
        dateLabel.classList.add('grid-label')
        //TODO adicionar formatação para melhor visualização de datas
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
    }

    //TODO ajuste para valor total não variar com base na filtragem
    document.getElementById('home-total-box').innerHTML = `Saldo total em conta <br>$ ${totalValue.toFixed(2)}`
}

function setDefaultDate() {
    let startDateInput = document.getElementById('start-date-input')
    startDateInput.max = new Date().toISOString().split("T")[0]
    let firstDayOfTheMonth = new Date()
    firstDayOfTheMonth.setDate(1)
    startDateInput.value = firstDayOfTheMonth.toISOString().split("T")[0]

    let endDateInput = document.getElementById('end-date-input')
    endDateInput.max = new Date().toISOString().split("T")[0]
    endDateInput.value = endDateInput.max

    startDateInput.addEventListener('change', updateTransactionList);
    endDateInput.addEventListener('change', updateTransactionList);
}

function updateTransactionList() {
    let list = document.getElementById('last-transaction-list')
    list.innerHTML = '';

    populateTransactionsList()
}