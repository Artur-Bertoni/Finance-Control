import { addDeleteIcon, doRequest, navigate, showToast } from '../utils/FrontendFunctions.js'
import { FinancialInstitution } from './class/FinancialInstitutionClass.js'
import { SidebarManager } from './components/SidebarManager.js'

SidebarManager.initialize()

const urlParams = new URLSearchParams(globalThis.location.search)
const fiId = urlParams.get('id')

if (fiId) {
    document.getElementById('page-title-text').textContent = 'Editar Instituição Financeira'
    document.getElementById('save-btn').textContent = 'Salvar Alterações'

    const response = doRequest(`/api/financial-institutions/${fiId}`, 'GET')
    if (response?.id !== undefined) {
        const fi = FinancialInstitution.processFinancialInstitution(response)
        document.getElementById('name-input').value    = fi.name    ?? ''
        document.getElementById('address-input').value = fi.address ?? ''
        document.getElementById('contact-input').value = fi.contact ?? ''

        const deleteBtn = addDeleteIcon()
        deleteBtn.addEventListener('click', function () {
            $.ajax({
                url:   `/api/financial-institutions/${fiId}`,
                type:  'DELETE',
                async: false,
                success: function () { navigate('/pages/FinancialInstitutionDashboard.html') },
                error:   function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao excluir instituição financeira.', 'error') }
            })
        })
    }
}

document.getElementById('cancel-btn').addEventListener('click', () =>
    navigate('/pages/FinancialInstitutionDashboard.html')
)

document.getElementById('save-btn').addEventListener('click', function () {
    const name = document.getElementById('name-input').value
    if (!name) {
        showToast('O campo Nome é obrigatório.', 'warning')
        return
    }

    const body = {
        name,
        address: document.getElementById('address-input').value || null,
        contact: document.getElementById('contact-input').value || null
    }

    $.ajax({
        url:         fiId ? `/api/financial-institutions/${fiId}` : '/api/financial-institutions',
        type:        fiId ? 'PUT' : 'POST',
        async:       false,
        contentType: 'application/json',
        data:        JSON.stringify(body),
        success:     function () { navigate('/pages/FinancialInstitutionDashboard.html') },
        error:       function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao salvar instituição financeira.', 'error') }
    })
})
