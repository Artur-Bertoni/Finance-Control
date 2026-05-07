import { Account } from './class/AccountClass.js'
import { TransactionLocale } from './class/TransactionLocaleClass.js'
import { clearDirtyGuard, doRequest, formatCurrency, navigate, navigateWithToast, setupDirtyGuard, showQuickAdd, showToast } from '../utils/FrontendFunctions.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'
import { I18n } from './i18n.js'

export function init() {
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

    // Populate category select for transfer
    const data = doRequest('/api/categories', 'GET') ?? []
    const catSel = document.getElementById('category-input')
    if (catSel) {
        data.forEach(c => {
            const opt = document.createElement('option')
            opt.value = c.id
            opt.text  = c.name
            catSel.appendChild(opt)
        })
    }

    const dateInput = document.getElementById('date-input')
    dateInput.max   = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().split('T')[0]
    dateInput.value = dateInput.max

    document.getElementById('origin-account-input').addEventListener('change', updateMaxValue)

    document.getElementById('cancel-btn').addEventListener('click', () =>
        navigate('/pages/AccountDashboard.html')
    )

    document.getElementById('save-btn').addEventListener('click', function () {
        const fieldLabels = {
            'origin-account-input':      I18n.t('sourceAccount'),
            'destination-account-input': I18n.t('destinationAccount'),
            'category-input':            I18n.t('categories'),
            'value-input':               I18n.t('transactionValue'),
            'date-input':                I18n.t('transactionDate')
        }

        const emptyFields = validateRequiredFields(
            ['origin-account-input', 'destination-account-input', 'category-input', 'value-input', 'date-input'],
            fieldLabels
        )

        if (emptyFields.length > 0) {
            showToast(I18n.t('fillRequiredFields', { fields: emptyFields.join(', ') }), 'warning')
            return
        }

        const localeId = document.getElementById('transfer-locale-input').value
        const body = {
            originAccountId:      Number(document.getElementById('origin-account-input').value),
            destinationAccountId: Number(document.getElementById('destination-account-input').value),
            categoryId:           Number(document.getElementById('category-input').value),
            transactionLocaleId:  localeId ? Number(localeId) : null,
            value:                Number(document.getElementById('value-input').value),
            date:                 document.getElementById('date-input').value,
            obs:                  document.getElementById('obs-input').value || null
        }

        $.ajax({
            url:         '/api/transfers',
            type:        'POST',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify(body),
            success:     function () {
                clearDirtyGuard()
                navigateWithToast('/pages/AccountDashboard.html', I18n.t('transferSuccess'), 'success')
            },
            error:       function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorTransfer'), 'error') }
        })
    })

    document.getElementById('origin-account-add-btn').addEventListener('click', () => {
        buildAccountQuickAdd(item => addOptionToSelects(['origin-account-input', 'destination-account-input'], item.id, item.name))
    })

    document.getElementById('dest-account-add-btn').addEventListener('click', () => {
        buildAccountQuickAdd(item => addOptionToSelects(['origin-account-input', 'destination-account-input'], item.id, item.name))
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
            onSuccess: item => addOptionToSelects(['category-input'], item.id, item.name)
        })
    })

    setupDirtyGuard()

    document.getElementById('locale-add-btn').addEventListener('click', () => {
        showQuickAdd({
            title:  I18n.t('newLocation'),
            apiUrl: '/api/transaction-locales',
            fields: [
                { id: 'name',    label: `${I18n.t('localeName')} *`, type: 'text', required: true, placeholder: I18n.t('localeNamePlaceholder') },
                { id: 'address', label: I18n.t('localeAddress'),       type: 'text', placeholder: I18n.t('localeAddressPlaceholder') }
            ],
            buildBody: v => ({ name: v.name, address: v.address || null }),
            onSuccess: item => addOptionToSelects(['transfer-locale-input'], item.id, item.name)
        })
    })
}

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
            valueInput.placeholder = I18n.t('maxValue', { max: formatCurrency(Number(total)) })
        },
        error: function () {}
    })
}

function buildAccountQuickAdd(onSuccess) {
    const fiOptions = (doRequest('/api/financial-institutions', 'GET') ?? [])
        .map(fi => ({ value: fi.id, label: fi.name }))

    showQuickAdd({
        title:  I18n.t('newAccount'),
        apiUrl: '/api/accounts',
        fields: [
            { id: 'name',  label: `${I18n.t('accountName')} *`, type: 'text', required: true, placeholder: I18n.t('accountNamePlaceholder') },
            { id: 'fiId', label: `${I18n.t('financialInstitution')} *`, type: 'select', required: true, options: fiOptions,
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
        onSuccess
    })
}

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

if (!globalThis.__appRouter) init()
