import { FinancialInstitution } from './class/FinancialInstitutionClass.js'
import { addDeleteIcon, doRequest, navigate, navigateWithToast, showQuickAdd, showToast } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()
    FinancialInstitution.addFinancialInstitutions()

    setupRequiredFieldValidation(['name-input', 'financial-institution-input', 'balance-input'])

    const accountId = new URLSearchParams(globalThis.location.search).get('id')
    if (accountId) loadEditMode(accountId)

    document.getElementById('cancel-btn').addEventListener('click', () =>
        navigate('/pages/AccountDashboard.html')
    )

    document.getElementById('save-btn').addEventListener('click', () => handleSave(accountId))

    document.getElementById('fi-add-btn').addEventListener('click', () => {
        showQuickAdd({
            title:  I18n.t('newFinancialInstitution'),
            apiUrl: '/api/financial-institutions',
            fields: [
                { id: 'name',    label: `${I18n.t('institutionName')} *`,    type: 'text', required: true, placeholder: I18n.t('institutionNamePlaceholder') },
                { id: 'address', label: I18n.t('institutionAddress'), type: 'text', placeholder: I18n.t('institutionAddressPlaceholder') },
                { id: 'contact', label: I18n.t('institutionContact'),  type: 'text', placeholder: I18n.t('institutionContactPlaceholder') }
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
}

function loadEditMode(accountId) {
    const titleEl = document.getElementById('page-title-text')
    if (titleEl) {
        titleEl.dataset.i18n = 'editAccount'
        titleEl.textContent  = I18n.t('editAccount')
    }
    const saveBtn = document.getElementById('save-btn')
    if (saveBtn) {
        saveBtn.dataset.i18n = 'saveChanges'
        saveBtn.textContent  = I18n.t('saveChanges')
    }

    const response = doRequest(`/api/accounts/${accountId}`, 'GET')
    if (response?.id === undefined) return

    const acc = Account.processAccount(response)
    document.getElementById('name-input').value        = acc.name ?? ''
    document.getElementById('contact-input').value     = acc.contact ?? ''
    document.getElementById('description-input').value = acc.description ?? ''
    document.getElementById('balance-input').value     = acc.balance === undefined ? '' : acc.balance.toFixed(2)

    selectOptionByText('financial-institution-input', acc.financialInstitution)

    const deleteBtn = addDeleteIcon()
    deleteBtn.addEventListener('click', () => {
        $.ajax({
            url:   `/api/accounts/${accountId}`,
            type:  'DELETE',
            async: false,
            success: () => navigate('/pages/AccountDashboard.html'),
            error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingAccount'), 'error')
        })
    })
}

function handleSave(accountId) {
    const name                   = document.getElementById('name-input').value
    const financialInstitutionId = document.getElementById('financial-institution-input').value
    const balance                = document.getElementById('balance-input').value

    const fieldLabels = {
        'name-input':                  I18n.t('accountName'),
        'financial-institution-input': I18n.t('financialInstitution'),
        'balance-input':               I18n.t('initialBalance')
    }

    const emptyFields = validateRequiredFields(
        ['name-input', 'financial-institution-input', 'balance-input'],
        fieldLabels
    )

    if (emptyFields.length > 0) {
        showToast(I18n.t('fillRequiredFields', { fields: emptyFields.join(', ') }), 'warning')
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
        success: () => {
            const msg = accountId ? I18n.t('accountUpdatedSuccess') : I18n.t('accountCreatedSuccess')
            navigateWithToast('/pages/AccountDashboard.html', msg, 'success')
        },
        error: xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingAccount'), 'error')
    })
}

function selectOptionByText(selectId, text) {
    for (const opt of document.getElementById(selectId).options) {
        if (opt.innerText === text) { opt.selected = true; break }
    }
}

if (!globalThis.__appRouter) init()
