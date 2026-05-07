import { addDeleteIcon, doRequest, navigate, navigateWithToast, setBreadcrumb, showQuickAdd, showToast } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { Category } from './class/CategoryClass.js'
import { TransactionLocale } from './class/TransactionLocaleClass.js'
import { Transaction } from './class/TransactionClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()

    setupRequiredFieldValidation(['account-input', 'category-input', 'debit-radio', 'date-input', 'value-input'])
    TransactionLocale.addTransactionLocales('transaction-locale-input')
    Account.addAccounts('account-input')
    Category.addCategories('category-input')

    const dateInput = document.getElementById('date-input')
    dateInput.max   = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().split('T')[0]
    dateInput.value = dateInput.max

    const transactionId = new URLSearchParams(globalThis.location.search).get('id')
    if (transactionId) loadEditMode(transactionId)

    document.querySelectorAll('input[name="typeRadio"]').forEach(r =>
        r.addEventListener('change', updateRadioStyle)
    )

    document.getElementById('cancel-btn').addEventListener('click', () =>
        navigate(transactionId ? `/pages/TransactionView.html?id=${transactionId}` : '/pages/HomePage.html')
    )

    document.getElementById('save-btn').addEventListener('click', () => handleSave(transactionId))

    setupQuickAddButtons()
}

function updateRadioStyle() {
    const isDebit  = document.getElementById('debit-radio').checked
    const isCredit = document.getElementById('credit-radio').checked
    document.getElementById('debit-option').classList.toggle('selected-debit',   isDebit)
    document.getElementById('credit-option').classList.toggle('selected-credit', isCredit)
}

function loadEditMode(transactionId) {
    const saveBtn = document.getElementById('save-btn')
    if (saveBtn) {
        saveBtn.dataset.i18n = 'saveChanges'
        saveBtn.textContent  = I18n.t('saveChanges')
    }

    const response = doRequest(`/api/transactions/${transactionId}`, 'GET')
    if (response?.id === undefined) return

    const tx = Transaction.processTransaction(response)

    setBreadcrumb([
        { label: I18n.t('movements'), url: '/pages/HomePage.html' },
        { label: formatTxLabel(tx), url: `/pages/TransactionView.html?id=${transactionId}` },
        { label: I18n.t('edit') }
    ])

    document.getElementById('debit-radio').checked  = tx.type === 'debit'
    document.getElementById('credit-radio').checked = tx.type === 'credit'
    updateRadioStyle()

    document.getElementById('date-input').value                        = tx.date
    document.getElementById('value-input').value                       = tx.value.toFixed(2)
    document.getElementById('installments-number-input').value         = tx.installmentsNumber
    document.getElementById('obs-input').value                         = tx.obs ?? ''
    document.getElementById('transfer-partner-id').value               = tx.transferPartnerId

    selectOptionByText('account-input',            tx.account)
    selectOptionByText('category-input',           tx.category)
    selectOptionByText('transaction-locale-input', tx.transactionLocale)

    const deleteBtn = addDeleteIcon()
    deleteBtn.addEventListener('click', () => {
        $.ajax({
            url:   `/api/transactions/${transactionId}`,
            type:  'DELETE',
            async: false,
            success: () => navigate('/pages/HomePage.html'),
            error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingTransaction'), 'error')
        })
    })
}

function handleSave(transactionId) {
    const fieldLabels = {
        'account-input':  I18n.t('transactionAccount'),
        'category-input': I18n.t('categories'),
        'debit-radio':    I18n.t('transactionType'),
        'date-input':     I18n.t('transactionDate'),
        'value-input':    I18n.t('transactionValue')
    }

    const emptyFields = validateRequiredFields(
        ['account-input', 'category-input', 'debit-radio', 'date-input', 'value-input'],
        fieldLabels
    )

    if (emptyFields.length > 0) {
        showToast(I18n.t('fillRequiredFields', { fields: emptyFields.join(', ') }), 'warning')
        return
    }

    const localeVal = document.getElementById('transaction-locale-input').value
    const body = {
        accountId:           Number(document.getElementById('account-input').value),
        categoryId:          Number(document.getElementById('category-input').value),
        transactionLocaleId: localeVal ? Number(localeVal) : null,
        value:               Number(document.getElementById('value-input').value),
        date:                document.getElementById('date-input').value,
        type:                document.getElementById('debit-radio').checked ? 'debit' : 'credit',
        installmentsNumber:  Number(document.getElementById('installments-number-input').value) || 0,
        obs:                 document.getElementById('obs-input').value || null,
        transferPartnerId:   Number(document.getElementById('transfer-partner-id').value) || null
    }

    $.ajax({
        url:         transactionId ? `/api/transactions/${transactionId}` : '/api/transactions',
        type:        transactionId ? 'PUT' : 'POST',
        async:       false,
        contentType: 'application/json',
        data:        JSON.stringify(body),
        success: () => {
            const msg = transactionId ? I18n.t('transactionUpdatedSuccess') : I18n.t('transactionCreatedSuccess')
            navigateWithToast('/pages/HomePage.html', msg, 'success')
        },
        error: xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingTransaction'), 'error')
    })
}

function setupQuickAddButtons() {
    document.getElementById('account-add-btn').addEventListener('click', () => {
        const fiOptions = (doRequest('/api/financial-institutions', 'GET') ?? [])
            .map(fi => ({ value: fi.id, label: fi.name }))

        showQuickAdd({
            title:  I18n.t('newAccount'),
            apiUrl: '/api/accounts',
            fields: [
                { id: 'name',    label: `${I18n.t('accountName')} *`, type: 'text', required: true, placeholder: I18n.t('accountNamePlaceholder') },
                { id: 'fiId',    label: `${I18n.t('financialInstitution')} *`, type: 'select', required: true, options: fiOptions,
                  placeholder: I18n.t('selectInstitution'),
                  addBtn: {
                    title: I18n.t('newFinancialInstitution'), apiUrl: '/api/financial-institutions',
                    fields: [
                        { id: 'name',    label: `${I18n.t('institutionName')} *`, type: 'text', required: true, placeholder: I18n.t('institutionNamePlaceholder') },
                        { id: 'address', label: I18n.t('institutionAddress'), type: 'text', placeholder: I18n.t('institutionAddressPlaceholder') },
                        { id: 'contact', label: I18n.t('institutionContact'),  type: 'text', placeholder: I18n.t('institutionContactPlaceholder') }
                    ],
                    buildBody: v => ({ name: v.name, address: v.address || null, contact: v.contact || null })
                  }
                },
                { id: 'balance', label: I18n.t('initialBalance'), type: 'number', placeholder: '0.00', step: '0.01' }
            ],
            buildBody: v => ({
                name: v.name, financialInstitutionId: Number(v.fiId),
                balance: v.balance === '' ? 0 : Number(v.balance), contact: null, description: null
            }),
            onSuccess: item => addOptionToSelect('account-input', item.id, item.name)
        })
    })

    document.getElementById('category-add-btn').addEventListener('click', () => {
        showQuickAdd({
            title:  I18n.t('newCategory'),
            apiUrl: '/api/categories',
            fields: [
                { id: 'name',        label: `${I18n.t('categoryName')} *`, type: 'text', required: true, placeholder: I18n.t('categoryNamePlaceholder') },
                { id: 'description', label: I18n.t('categoryDescription'),  type: 'textarea', placeholder: I18n.t('categoryDescriptionPlaceholder') }
            ],
            buildBody: v => ({ name: v.name, description: v.description || null }),
            onSuccess: item => addOptionToSelect('category-input', item.id, item.name)
        })
    })

    document.getElementById('locale-add-btn').addEventListener('click', () => {
        showQuickAdd({
            title:  I18n.t('newLocation'),
            apiUrl: '/api/transaction-locales',
            fields: [
                { id: 'name',    label: `${I18n.t('localeName')} *`, type: 'text', required: true, placeholder: I18n.t('localeNamePlaceholder') },
                { id: 'address', label: I18n.t('localeAddress'),      type: 'text', placeholder: I18n.t('localeAddressPlaceholder') }
            ],
            buildBody: v => ({ name: v.name, address: v.address || null }),
            onSuccess: item => addOptionToSelect('transaction-locale-input', item.id, item.name)
        })
    })
}

function formatTxLabel(tx) {
    const d = new Date(tx.date)
    const dateStr = `${d.getUTCDate().toString().padStart(2,'0')}/${(d.getUTCMonth()+1).toString().padStart(2,'0')}/${d.getUTCFullYear()}`
    return `${tx.category} – ${dateStr}`
}

function selectOptionByText(selectId, text) {
    for (const opt of document.getElementById(selectId).options) {
        if (opt.innerText === text) { opt.selected = true; break }
    }
}

function addOptionToSelect(selectId, value, label) {
    const sel = document.getElementById(selectId)
    if (!sel) return
    const opt = document.createElement('option')
    opt.value = value
    opt.text  = label
    sel.appendChild(opt)
    sel.value = value
}

if (!globalThis.__appRouter) init()
