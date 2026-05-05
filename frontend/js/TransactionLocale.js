import { addDeleteIcon, doRequest, navigate, showToast } from '../utils/FrontendFunctions.js'
import { TransactionLocale } from './class/TransactionLocaleClass.js'
import { SidebarManager } from './components/SidebarManager.js'

SidebarManager.initialize()

const urlParams = new URLSearchParams(window.location.search)
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
    if (!name) {
        showToast('O campo Nome é obrigatório.', 'warning')
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
        success:     function () { navigate('/pages/TransactionLocaleDashboard.html') },
        error:       function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao salvar local.', 'error') }
    })
})
