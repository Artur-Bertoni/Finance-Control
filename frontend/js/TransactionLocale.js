import {addDeleteIcon, doRequest, navigate} from "../utils/FrontendFunctions.js"
import {TransactionLocale} from "./class/TransactionLocaleClass.js"

const urlParams = new URLSearchParams(window.location.search)
const localeId = urlParams.get('id')

document.querySelector('form[name=form]').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'cancelButton') navigate('/pages/TransactionLocaleDashboard.html')
})

if (localeId) {
    let response = doRequest(`/api/transaction-locales/${localeId}`, 'GET')
    if (response?.id !== undefined) {
        let locale = TransactionLocale.processTransactionLocale(response)
        document.getElementById('name-input').value = locale.name ?? ''
        document.getElementById('address-input').value = locale.address ?? ''

        let deleteBtn = addDeleteIcon()
        deleteBtn.addEventListener('click', function () {
            $.ajax({
                url: `/api/transaction-locales/${localeId}`,
                type: 'DELETE',
                async: false,
                success: function () { navigate('/pages/TransactionLocaleDashboard.html') },
                error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao excluir local.') }
            })
        })
    }
}

document.getElementById('save-btn').addEventListener('click', function () {
    let name = document.getElementById('name-input').value

    if (!name) {
        alert('O campo Nome deve ser preenchido!')
        return
    }

    let body = {
        name,
        address: document.getElementById('address-input').value || null
    }

    $.ajax({
        url: localeId ? `/api/transaction-locales/${localeId}` : '/api/transaction-locales',
        type: localeId ? 'PUT' : 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify(body),
        success: function () { navigate('/pages/TransactionLocaleDashboard.html') },
        error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao salvar local.') }
    })
})
