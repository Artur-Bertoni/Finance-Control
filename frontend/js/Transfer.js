import {Account} from "./class/AccountClass.js"
import {Category} from "./class/CategoryClass.js"
import {TransactionLocale} from "./class/TransactionLocaleClass.js"
import {navigate} from "../utils/FrontendFunctions.js"

Category.addCategories('category-input')
TransactionLocale.addTransactionLocales('transfer-locale-input')
Account.addAccounts('origin-account-input')
Account.addAccounts('destination-account-input')

let date = document.getElementById('date-input')
date.max = new Date().toISOString().split("T")[0]
date.value = date.max

document.getElementById('origin-account-input').addEventListener('change', setMaxValue)

document.querySelector('form[name=form]').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'cancelButton') navigate('/pages/AccountDashboard.html')
})

document.getElementById('save-btn').addEventListener('click', function () {
    let originAccountId = document.getElementById('origin-account-input').value
    let destinationAccountId = document.getElementById('destination-account-input').value
    let categoryId = document.getElementById('category-input').value
    let value = document.getElementById('value-input').value
    let dateValue = document.getElementById('date-input').value

    if (!originAccountId || !destinationAccountId || !categoryId || !dateValue || !value) {
        alert('Os campos Conta de Origem, Conta Destino, Categoria, Valor e Data devem ser preenchidos!')
        return
    }

    let localeId = document.getElementById('transfer-locale-input').value

    let body = {
        originAccountId: Number(originAccountId),
        destinationAccountId: Number(destinationAccountId),
        categoryId: Number(categoryId),
        transactionLocaleId: localeId ? Number(localeId) : null,
        value: Number(value),
        date: dateValue,
        obs: document.getElementById('obs-input').value || null
    }

    $.ajax({
        url: '/api/transfers',
        type: 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify(body),
        success: function () { navigate('/pages/AccountDashboard.html') },
        error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao realizar transferência.') }
    })
})

function setMaxValue() {
    let accountId = document.getElementById('origin-account-input').value
    if (!accountId) return

    $.ajax({
        url: `/api/accounts/total-value?accountId=${accountId}`,
        type: 'GET',
        async: false,
        success: function (total) {
            let valueInput = document.getElementById('value-input')
            valueInput.max = total
            valueInput.placeholder = `Valor ($) - Max: $ ${Number(total).toFixed(2)}`
        },
        error: function () {}
    })
}
