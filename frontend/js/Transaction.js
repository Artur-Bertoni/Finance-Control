import {addDeleteIcon, doRequest, navigate} from "../utils/FrontendFunctions.js"
import {Account} from "./class/AccountClass.js"
import {Category} from "./class/CategoryClass.js"
import {TransactionLocale} from "./class/TransactionLocaleClass.js"
import {Transaction} from "./class/TransactionClass.js"

Category.addCategories('category-input')
TransactionLocale.addTransactionLocales('transaction-locale-input')
Account.addAccounts('account-input')

let date = document.getElementById('date-input')
date.max = new Date().toISOString().split("T")[0]
date.value = date.max

const urlParams = new URLSearchParams(window.location.search)
const transactionId = urlParams.get('id')

document.querySelector('form[name=form]').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'cancelButton') navigate('/pages/HomePage.html')
})

if (transactionId) {
    let response = doRequest(`/api/transactions/${transactionId}`, 'GET')
    if (response?.id !== undefined) {
        let transaction = Transaction.processTransaction(response)

        let typeRadioDebit = document.querySelector('input[name="typeRadio"][value="debit"]')
        let typeRadioCredit = document.querySelector('input[name="typeRadio"][value="credit"]')
        typeRadioDebit.checked = transaction.type === 'debit'
        typeRadioCredit.checked = transaction.type === 'credit'

        document.getElementById('date-input').value = transaction.date
        document.getElementById('value-input').value = transaction.value.toFixed(2)
        document.getElementById('installments-number-input').value = transaction.installmentsNumber
        document.getElementById('obs-input').value = transaction.obs ?? ''
        document.getElementById('transfer-partner-id').value = transaction.transferPartnerId

        let accountInput = document.getElementById('account-input')
        for (const option of accountInput.options) {
            if (option.innerText === transaction.account) { option.selected = true; break }
        }

        let categoryInput = document.getElementById('category-input')
        for (const option of categoryInput.options) {
            if (option.innerText === transaction.category) { option.selected = true; break }
        }

        let localeInput = document.getElementById('transaction-locale-input')
        for (const option of localeInput.options) {
            if (option.innerText === transaction.transactionLocale) { option.selected = true; break }
        }

        let deleteBtn = addDeleteIcon()
        deleteBtn.addEventListener('click', function () {
            $.ajax({
                url: `/api/transactions/${transactionId}`,
                type: 'DELETE',
                async: false,
                success: function () { navigate('/pages/HomePage.html') },
                error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao excluir transação.') }
            })
        })
    }
}

document.getElementById('save-btn').addEventListener('click', function () {
    let accountId = document.getElementById('account-input').value
    let categoryId = document.getElementById('category-input').value
    let typeRadioDebit = document.getElementById('debit-radio')
    let typeRadioCredit = document.getElementById('credit-radio')
    let dateValue = document.getElementById('date-input').value
    let value = document.getElementById('value-input').value

    if (!accountId || !categoryId || (!typeRadioDebit.checked && !typeRadioCredit.checked) || !dateValue || !value) {
        alert('Os campos Conta, Categoria, Tipo de Transação, Data e Valor devem ser preenchidos!')
        return
    }

    let localeId = document.getElementById('transaction-locale-input').value
    let transferPartnerId = Number(document.getElementById('transfer-partner-id').value) || null

    let body = {
        accountId: Number(accountId),
        categoryId: Number(categoryId),
        transactionLocaleId: localeId ? Number(localeId) : null,
        value: Number(value),
        date: dateValue,
        type: typeRadioDebit.checked ? 'debit' : 'credit',
        installmentsNumber: Number(document.getElementById('installments-number-input').value) || 0,
        obs: document.getElementById('obs-input').value || null,
        transferPartnerId
    }

    $.ajax({
        url: transactionId ? `/api/transactions/${transactionId}` : '/api/transactions',
        type: transactionId ? 'PUT' : 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify(body),
        success: function () { navigate('/pages/HomePage.html') },
        error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao salvar transação.') }
    })
})
