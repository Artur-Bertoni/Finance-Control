import {addDeleteIcon, doRequest, navigate} from "../utils/FrontendFunctions.js"
import {FinancialInstitution} from "./class/FinancialInstitutionClass.js"

const urlParams = new URLSearchParams(window.location.search)
const fiId = urlParams.get('id')

document.querySelector('form[name=form]').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'cancelButton') navigate('/pages/FinancialInstitutionDashboard.html')
})

if (fiId) {
    let response = doRequest(`/api/financial-institutions/${fiId}`, 'GET')
    if (response?.id !== undefined) {
        let fi = FinancialInstitution.processFinancialInstitution(response)
        document.getElementById('name-input').value = fi.name ?? ''
        document.getElementById('address-input').value = fi.address ?? ''
        document.getElementById('contact-input').value = fi.contact ?? ''

        let deleteBtn = addDeleteIcon()
        deleteBtn.addEventListener('click', function () {
            $.ajax({
                url: `/api/financial-institutions/${fiId}`,
                type: 'DELETE',
                async: false,
                success: function () { navigate('/pages/FinancialInstitutionDashboard.html') },
                error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao excluir instituição financeira.') }
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
        address: document.getElementById('address-input').value || null,
        contact: document.getElementById('contact-input').value || null
    }

    $.ajax({
        url: fiId ? `/api/financial-institutions/${fiId}` : '/api/financial-institutions',
        type: fiId ? 'PUT' : 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify(body),
        success: function () { navigate('/pages/FinancialInstitutionDashboard.html') },
        error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao salvar instituição financeira.') }
    })
})
