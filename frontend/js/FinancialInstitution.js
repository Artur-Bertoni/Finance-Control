import { addDeleteIcon, doRequest, navigate, navigateWithToast, showToast } from '../utils/FrontendFunctions.js'
import { FinancialInstitution } from './class/FinancialInstitutionClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'

SidebarManager.initialize()

setupRequiredFieldValidation(['name-input'])

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

    const fieldLabels = {
      'name-input': 'Nome'
    }

    const emptyFields = validateRequiredFields(['name-input'], fieldLabels)

    if (emptyFields.length > 0) {
        showToast(`Preencha os campos obrigatórios: ${emptyFields.join(', ')}.`, 'warning')
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
        success:     function () {
            const msg = fiId ? 'Instituição Financeira atualizada com sucesso!' : 'Instituição Financeira criada com sucesso!'
            navigateWithToast('/pages/FinancialInstitutionDashboard.html', msg, 'success')
        },
        error:       function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao salvar Instituição Financeira.', 'error') }
    })
})
