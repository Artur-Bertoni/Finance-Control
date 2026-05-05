import {FinancialInstitution} from "./class/FinancialInstitutionClass.js"
import {addDeleteIcon, doRequest, navigate} from "../utils/FrontendFunctions.js"
import {Account} from "./class/AccountClass.js"

FinancialInstitution.addFinancialInstitutions()

const urlParams = new URLSearchParams(window.location.search)
const accountId = urlParams.get('id')

document.querySelector('form[name=form]').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'cancelButton') navigate('/pages/AccountDashboard.html')
})

if (accountId) {
    let response = doRequest(`/api/accounts/${accountId}`, 'GET')
    if (response?.id !== undefined) {
        let account = Account.processAccount(response)
        document.getElementById('name-input').value = account.name ?? ''
        document.getElementById('contact-input').value = account.contact ?? ''
        document.getElementById('description-input').value = account.description ?? ''
        document.getElementById('balance-input').value = account.balance !== undefined ? account.balance.toFixed(2) : ''

        let financialInstitutionInput = document.getElementById('financial-institution-input')
        for (const option of financialInstitutionInput.options) {
            if (option.innerText === account.financialInstitution) {
                option.selected = true
                break
            }
        }

        let deleteBtn = addDeleteIcon()
        deleteBtn.addEventListener('click', function () {
            $.ajax({
                url: `/api/accounts/${accountId}`,
                type: 'DELETE',
                async: false,
                success: function () { navigate('/pages/AccountDashboard.html') },
                error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao excluir conta.') }
            })
        })
    }
}

document.getElementById('save-btn').addEventListener('click', function () {
    let name = document.getElementById('name-input').value
    let financialInstitutionId = document.getElementById('financial-institution-input').value
    let balance = document.getElementById('balance-input').value

    if (!name || !financialInstitutionId || balance === '') {
        alert('Os campos Nome, Instituição Financeira e Saldo devem ser preenchidos!')
        return
    }

    let body = {
        name,
        financialInstitutionId: Number(financialInstitutionId),
        contact: document.getElementById('contact-input').value || null,
        description: document.getElementById('description-input').value || null,
        balance: Number(balance)
    }

    $.ajax({
        url: accountId ? `/api/accounts/${accountId}` : '/api/accounts',
        type: accountId ? 'PUT' : 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify(body),
        success: function () { navigate('/pages/AccountDashboard.html') },
        error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao salvar conta.') }
    })
})
