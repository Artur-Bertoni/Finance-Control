import { Account } from './class/AccountClass.js'
import { TransactionLocale } from './class/TransactionLocaleClass.js'
import { doRequest, navigate, navigateWithToast, showQuickAdd, showToast } from '../utils/FrontendFunctions.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'

SidebarManager.initialize()

setupRequiredFieldValidation([
  'origin-account-input',
  'destination-account-input',
  'category-input',
  'value-input',
  'date-input'
])
TransactionLocale.addTransactionLocales('transfer-locale-input')
Account.addAccounts('origin-account-input')
Account.addAccounts('destination-account-input')

const dateInput = document.getElementById('date-input')
dateInput.max   = new Date().toISOString().split('T')[0]
dateInput.value = dateInput.max

document.getElementById('origin-account-input').addEventListener('change', updateMaxValue)

document.getElementById('cancel-btn').addEventListener('click', () =>
    navigate('/pages/AccountDashboard.html')
)

document.getElementById('save-btn').addEventListener('click', function () {
    const originAccountId      = document.getElementById('origin-account-input').value
    const destinationAccountId = document.getElementById('destination-account-input').value
    const categoryId           = document.getElementById('category-input').value
    const value                = document.getElementById('value-input').value
    const dateValue            = document.getElementById('date-input').value

    const requiredFields = [
      'origin-account-input',
      'destination-account-input',
      'category-input',
      'value-input',
      'date-input'
    ]

    const fieldLabels = {
      'origin-account-input': 'Conta de Origem',
      'destination-account-input': 'Conta de Destino',
      'category-input': 'Categoria',
      'value-input': 'Valor',
      'date-input': 'Data'
    }

    const emptyFields = validateRequiredFields(requiredFields, fieldLabels)

    if (emptyFields.length > 0) {
        showToast(`Preencha os campos obrigatórios: ${emptyFields.join(', ')}.`, 'warning')
        return
    }

    const localeId = document.getElementById('transfer-locale-input').value

    const body = {
        originAccountId:      Number(originAccountId),
        destinationAccountId: Number(destinationAccountId),
        categoryId:           Number(categoryId),
        transactionLocaleId:  localeId ? Number(localeId) : null,
        value:                Number(value),
        date:                 dateValue,
        obs:                  document.getElementById('obs-input').value || null
    }

    $.ajax({
        url:         '/api/transfers',
        type:        'POST',
        async:       false,
        contentType: 'application/json',
        data:        JSON.stringify(body),
        success:     function () {
            navigateWithToast('/pages/AccountDashboard.html', 'Transferência realizada com sucesso!', 'success')
        },
        error:       function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao realizar transferência.', 'error') }
    })
})

function updateMaxValue() {
    const accountId = document.getElementById('origin-account-input').value
    if (!accountId) return

    $.ajax({
        url:   `/api/accounts/total-value?accountId=${accountId}`,
        type:  'GET',
        async: false,
        success: function (total) {
            const valueInput       = document.getElementById('value-input')
            valueInput.max         = total
            valueInput.placeholder = `Máx: $ ${Number(total).toFixed(2)}`
        },
        error: function () {}
    })
}

// ── Quick-add buttons ──────────────────────────────────────────────────────

function buildAccountQuickAdd(onSuccess) {
    const fiOptions = (doRequest('/api/financial-institutions', 'GET') ?? [])
        .map(fi => ({ value: fi.id, label: fi.name }))

    showQuickAdd({
        title:  'Nova Conta',
        apiUrl: '/api/accounts',
        fields: [
            { id: 'name',    label: 'Nome *',                  type: 'text',   required: true, placeholder: 'Nome da conta' },
            { id: 'fiId', label: 'Instituição Financeira *', type: 'select', required: true, options: fiOptions, placeholder: 'Selecione',
              addBtn: { title: 'Nova Instituição Financeira', apiUrl: '/api/financial-institutions',
                fields: [
                    { id: 'name',    label: 'Nome *',   type: 'text', required: true, placeholder: 'Nome da instituição' },
                    { id: 'address', label: 'Endereço', type: 'text', placeholder: 'Endereço (opcional)' },
                    { id: 'contact', label: 'Contato',  type: 'text', placeholder: 'Telefone ou e-mail (opcional)' }
                ],
                buildBody: v => ({ name: v.name, address: v.address || null, contact: v.contact || null })
              }
            },
            { id: 'balance', label: 'Saldo Inicial',            type: 'number', placeholder: '0.00', step: '0.01' }
        ],
        buildBody: v => ({
            name:                   v.name,
            financialInstitutionId: Number(v.fiId),
            balance:                v.balance === '' ? 0 : Number(v.balance),
            contact:                null,
            description:            null
        }),
        onSuccess
    })
}

document.getElementById('origin-account-add-btn').addEventListener('click', () => {
    buildAccountQuickAdd(item => {
        addOptionToSelects(['origin-account-input', 'destination-account-input'], item.id, item.name)
    })
})

document.getElementById('dest-account-add-btn').addEventListener('click', () => {
    buildAccountQuickAdd(item => {
        addOptionToSelects(['origin-account-input', 'destination-account-input'], item.id, item.name)
    })
})

document.getElementById('category-add-btn').addEventListener('click', () => {
    showQuickAdd({
        title:  'Nova Categoria',
        apiUrl: '/api/categories',
        fields: [
            { id: 'name',        label: 'Nome *',    type: 'text',     required: true, placeholder: 'Nome da categoria' },
            { id: 'description', label: 'Descrição', type: 'textarea', placeholder: 'Descrição (opcional)' }
        ],
        buildBody: v => ({ name: v.name, description: v.description || null }),
        onSuccess: item => addOptionToSelects(['category-input'], item.id, item.name)
    })
})

document.getElementById('locale-add-btn').addEventListener('click', () => {
    showQuickAdd({
        title:  'Novo Local',
        apiUrl: '/api/transaction-locales',
        fields: [
            { id: 'name',    label: 'Nome *',   type: 'text', required: true, placeholder: 'Nome do local' },
            { id: 'address', label: 'Endereço', type: 'text', placeholder: 'Endereço (opcional)' }
        ],
        buildBody: v => ({ name: v.name, address: v.address || null }),
        onSuccess: item => addOptionToSelects(['transfer-locale-input'], item.id, item.name)
    })
})

function addOptionToSelects(selectIds, value, label) {
    selectIds.forEach(id => {
        const sel = document.getElementById(id)
        if (!sel) return
        const opt = document.createElement('option')
        opt.value = value
        opt.text  = label
        sel.appendChild(opt)
        sel.value = value
    })
}
