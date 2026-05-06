import { FinancialInstitution } from './class/FinancialInstitutionClass.js'
import { addDeleteIcon, doRequest, navigate, showQuickAdd, showToast } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'

SidebarManager.initialize()
FinancialInstitution.addFinancialInstitutions()

const urlParams = new URLSearchParams(globalThis.location.search)
const accountId = urlParams.get('id')

if (accountId) {
    document.getElementById('page-title-text').textContent = 'Editar Conta'
    document.getElementById('save-btn').textContent = 'Salvar Alterações'

    const response = doRequest(`/api/accounts/${accountId}`, 'GET')
    if (response?.id !== undefined) {
        const acc = Account.processAccount(response)
        document.getElementById('name-input').value        = acc.name ?? ''
        document.getElementById('contact-input').value     = acc.contact ?? ''
        document.getElementById('description-input').value = acc.description ?? ''
        document.getElementById('balance-input').value     = acc.balance === undefined ? '' : acc.balance.toFixed(2)

        const fiSel = document.getElementById('financial-institution-input')
        for (const opt of fiSel.options) {
            if (opt.innerText === acc.financialInstitution) { opt.selected = true; break }
        }

        const deleteBtn = addDeleteIcon()
        deleteBtn.addEventListener('click', function () {
            $.ajax({
                url:   `/api/accounts/${accountId}`,
                type:  'DELETE',
                async: false,
                success: function () { navigate('/pages/AccountDashboard.html') },
                error:   function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao excluir conta.', 'error') }
            })
        })
    }
}

document.getElementById('cancel-btn').addEventListener('click', () =>
    navigate('/pages/AccountDashboard.html')
)

document.getElementById('save-btn').addEventListener('click', function () {
    const name                = document.getElementById('name-input').value
    const financialInstitutionId = document.getElementById('financial-institution-input').value
    const balance             = document.getElementById('balance-input').value

    if (!name || !financialInstitutionId || balance === '') {
        showToast('Preencha os campos obrigatórios: Nome, Instituição Financeira e Saldo.', 'warning')
        return
    }

    const body = {
        name,
        financialInstitutionId: Number(financialInstitutionId),
        contact:     document.getElementById('contact-input').value     || null,
        description: document.getElementById('description-input').value || null,
        balance:     Number(balance)
    }

    $.ajax({
        url:         accountId ? `/api/accounts/${accountId}` : '/api/accounts',
        type:        accountId ? 'PUT' : 'POST',
        async:       false,
        contentType: 'application/json',
        data:        JSON.stringify(body),
        success:     function () { navigate('/pages/AccountDashboard.html') },
        error:       function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao salvar conta.', 'error') }
    })
})

document.getElementById('fi-add-btn').addEventListener('click', () => {
    showQuickAdd({
        title:  'Nova Instituição Financeira',
        apiUrl: '/api/financial-institutions',
        fields: [
            { id: 'name',    label: 'Nome *',    type: 'text', required: true, placeholder: 'Nome da instituição' },
            { id: 'address', label: 'Endereço',  type: 'text', placeholder: 'Endereço (opcional)' },
            { id: 'contact', label: 'Contato',   type: 'text', placeholder: 'Telefone ou e-mail (opcional)' }
        ],
        buildBody: v => ({ name: v.name, address: v.address || null, contact: v.contact || null }),
        onSuccess: item => {
            const sel = document.getElementById('financial-institution-input')
            const opt = document.createElement('option')
            opt.value = item.id
            opt.text  = item.name
            sel.appendChild(opt)
            sel.value = item.id
        }
    })
})
