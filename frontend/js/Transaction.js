import { addDeleteIcon, doRequest, navigate, navigateWithToast, showQuickAdd, showToast } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { Category } from './class/CategoryClass.js'
import { TransactionLocale } from './class/TransactionLocaleClass.js'
import { Transaction } from './class/TransactionClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'

SidebarManager.initialize()

setupRequiredFieldValidation([
  'account-input',
  'category-input',
  'debit-radio',
  'date-input',
  'value-input'
])
TransactionLocale.addTransactionLocales('transaction-locale-input')
Account.addAccounts('account-input')
Category.addCategories('category-input')

const dateInput = document.getElementById('date-input')
dateInput.max   = new Date().toISOString().split('T')[0]
dateInput.value = dateInput.max

const urlParams     = new URLSearchParams(globalThis.location.search)
const transactionId = urlParams.get('id')

if (transactionId) {
    document.getElementById('page-title-text').textContent = 'Editar Transação'
    document.getElementById('save-btn').textContent = 'Salvar Alterações'

    const response = doRequest(`/api/transactions/${transactionId}`, 'GET')
    if (response?.id !== undefined) {
        const tx = Transaction.processTransaction(response)

        document.getElementById('debit-radio').checked  = tx.type === 'debit'
        document.getElementById('credit-radio').checked = tx.type === 'credit'
        updateRadioStyle()

        dateInput.value = tx.date
        document.getElementById('value-input').value                 = tx.value.toFixed(2)
        document.getElementById('installments-number-input').value   = tx.installmentsNumber
        document.getElementById('obs-input').value                   = tx.obs ?? ''
        document.getElementById('transfer-partner-id').value         = tx.transferPartnerId

        const accountSel = document.getElementById('account-input')
        for (const opt of accountSel.options) {
            if (opt.innerText === tx.account) { opt.selected = true; break }
        }
        const categorySel = document.getElementById('category-input')
        for (const opt of categorySel.options) {
            if (opt.innerText === tx.category) { opt.selected = true; break }
        }
        const localeSel = document.getElementById('transaction-locale-input')
        for (const opt of localeSel.options) {
            if (opt.innerText === tx.transactionLocale) { opt.selected = true; break }
        }

        const deleteBtn = addDeleteIcon()
        deleteBtn.addEventListener('click', function () {
            $.ajax({
                url: `/api/transactions/${transactionId}`,
                type: 'DELETE',
                async: false,
                success: function () { navigate('/pages/HomePage.html') },
                error: function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao excluir transação.', 'error') }
            })
        })
    }
}

document.querySelectorAll('input[name="typeRadio"]').forEach(r =>
    r.addEventListener('change', updateRadioStyle)
)

function updateRadioStyle() {
    const debitOpt  = document.getElementById('debit-option')
    const creditOpt = document.getElementById('credit-option')
    const isDebit   = document.getElementById('debit-radio').checked
    const isCredit  = document.getElementById('credit-radio').checked
    debitOpt.classList.toggle('selected-debit',   isDebit)
    creditOpt.classList.toggle('selected-credit', isCredit)
}

document.getElementById('cancel-btn').addEventListener('click', () =>
    navigate('/pages/HomePage.html')
)

document.getElementById('save-btn').addEventListener('click', function () {
    const accountId  = document.getElementById('account-input').value
    const categoryId = document.getElementById('category-input').value
    const debitRadio  = document.getElementById('debit-radio')
    const dateValue  = document.getElementById('date-input').value
    const value      = document.getElementById('value-input').value

    const requiredFields = [
      'account-input',
      'category-input',
      'debit-radio',
      'date-input',
      'value-input'
    ]

    const fieldLabels = {
      'account-input': 'Conta',
      'category-input': 'Categoria',
      'debit-radio': 'Tipo',
      'date-input': 'Data',
      'value-input': 'Valor'
    }

    const emptyFields = validateRequiredFields(requiredFields, fieldLabels)

    if (emptyFields.length > 0) {
        showToast(`Preencha os campos obrigatórios: ${emptyFields.join(', ')}.`, 'warning')
        return
    }

    const localeId          = document.getElementById('transaction-locale-input').value
    const transferPartnerId = Number(document.getElementById('transfer-partner-id').value) || null

    const body = {
        accountId:           Number(accountId),
        categoryId:          Number(categoryId),
        transactionLocaleId: localeId ? Number(localeId) : null,
        value:               Number(value),
        date:                dateValue,
        type:                debitRadio.checked ? 'debit' : 'credit',
        installmentsNumber:  Number(document.getElementById('installments-number-input').value) || 0,
        obs:                 document.getElementById('obs-input').value || null,
        transferPartnerId
    }

    $.ajax({
        url:         transactionId ? `/api/transactions/${transactionId}` : '/api/transactions',
        type:        transactionId ? 'PUT' : 'POST',
        async:       false,
        contentType: 'application/json',
        data:        JSON.stringify(body),
        success:     function () {
            const msg = transactionId ? 'Transação atualizada com sucesso!' : 'Transação criada com sucesso!'
            navigateWithToast('/pages/HomePage.html', msg, 'success')
        },
        error:       function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao salvar transação.', 'error') }
    })
})

// ── Quick-add buttons ──────────────────────────────────────────────────────

document.getElementById('account-add-btn').addEventListener('click', () => {
    const fiOptions = (doRequest('/api/financial-institutions', 'GET') ?? [])
        .map(fi => ({ value: fi.id, label: fi.name }))

    showQuickAdd({
        title:  'Nova Conta',
        apiUrl: '/api/accounts',
        fields: [
            { id: 'name',    label: 'Nome *',                 type: 'text',   required: true, placeholder: 'Nome da conta' },
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
            { id: 'balance', label: 'Saldo Inicial',           type: 'number', placeholder: '0.00', step: '0.01' }
        ],
        buildBody: v => ({
            name:                   v.name,
            financialInstitutionId: Number(v.fiId),
            balance:                v.balance === '' ? 0 : Number(v.balance),
            contact:                null,
            description:            null
        }),
        onSuccess: item => addOptionToSelect('account-input', item.id, item.name)
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
        onSuccess: item => addOptionToSelect('category-input', item.id, item.name)
    })
})

document.getElementById('locale-add-btn').addEventListener('click', () => {
    showQuickAdd({
        title:  'Novo Local',
        apiUrl: '/api/transaction-locales',
        fields: [
            { id: 'name',    label: 'Nome *',    type: 'text', required: true, placeholder: 'Nome do local' },
            { id: 'address', label: 'Endereço',  type: 'text', placeholder: 'Endereço (opcional)' }
        ],
        buildBody: v => ({ name: v.name, address: v.address || null }),
        onSuccess: item => addOptionToSelect('transaction-locale-input', item.id, item.name)
    })
})

function addOptionToSelect(selectId, value, label) {
    const sel = document.getElementById(selectId)
    const opt = document.createElement('option')
    opt.value = value
    opt.text  = label
    sel.appendChild(opt)
    sel.value = value
}
