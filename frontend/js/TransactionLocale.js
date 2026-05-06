import { addDeleteIcon, doRequest, navigate, navigateWithToast, showToast } from '../utils/FrontendFunctions.js'
import { TransactionLocale } from './class/TransactionLocaleClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'

SidebarManager.initialize()

setupRequiredFieldValidation(['name-input'])

const urlParams = new URLSearchParams(globalThis.location.search)
const localeId = urlParams.get('id')

if (localeId) {
    document.getElementById('page-title-text').textContent = 'Editar Local'
    document.getElementById('save-btn').textContent = 'Salvar Alterações'

    const response = doRequest(`/api/transaction-locales/${localeId}`, 'GET')
    if (response?.id !== undefined) {
        const locale = TransactionLocale.processTransactionLocale(response)
        document.getElementById('name-input').value    = locale.name    ?? ''
        document.getElementById('address-input').value = locale.address ?? ''

        const deleteBtn = addDeleteIcon()
        deleteBtn.addEventListener('click', function () {
            $.ajax({
                url:   `/api/transaction-locales/${localeId}`,
                type:  'DELETE',
                async: false,
                success: function () { navigate('/pages/TransactionLocaleDashboard.html') },
                error:   function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao excluir local.', 'error') }
            })
        })
    }
}

document.getElementById('cancel-btn').addEventListener('click', () =>
    navigate('/pages/TransactionLocaleDashboard.html')
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
        address: document.getElementById('address-input').value || null
    }

    $.ajax({
        url:         localeId ? `/api/transaction-locales/${localeId}` : '/api/transaction-locales',
        type:        localeId ? 'PUT' : 'POST',
        async:       false,
        contentType: 'application/json',
        data:        JSON.stringify(body),
        success:     function () {
            const msg = localeId ? 'Local atualizado com sucesso!' : 'Local criado com sucesso!'
            navigateWithToast('/pages/TransactionLocaleDashboard.html', msg, 'success')
        },
        error:       function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao salvar local.', 'error') }
    })
})
