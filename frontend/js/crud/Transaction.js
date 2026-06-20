import { addDeleteIcon, addOptionToSelect, clearDirtyGuard, doRequest, navigate, navigateWithToast, selectOptionByText, setBreadcrumb, setupDirtyGuard, showConfirmAsync, showQuickAdd, showToast } from '../../utils/FrontendFunctions.js'
import { Account } from '../class/AccountClass.js'
import { Category } from '../class/CategoryClass.js'
import { TransactionLocale } from '../class/TransactionLocaleClass.js'
import { Transaction } from '../class/TransactionClass.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from '../utils/FieldValidation.js'
import { I18n } from '../i18n.js'

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
        navigate(transactionId ? `/pages/views/TransactionView.html?id=${transactionId}` : '/pages/HomePage.html')
    )

    document.getElementById('save-btn').addEventListener('click', () => handleSave(transactionId, false))

    setupQuickAddButtons()
    setupMoreOptions()
    setupDirtyGuard()
}

function setupMoreOptions() {
    const toggle = document.getElementById('tx-more-toggle')
    const panel  = document.getElementById('tx-more-options')
    if (!toggle || !panel || toggle.dataset.bound) return
    toggle.dataset.bound = '1'
    toggle.addEventListener('click', () => {
        const open = panel.hidden
        panel.hidden = !open
        toggle.setAttribute('aria-expanded', String(open))
        toggle.classList.toggle('form-more-toggle--open', open)
    })
}

function openMoreOptions() {
    const toggle = document.getElementById('tx-more-toggle')
    const panel  = document.getElementById('tx-more-options')
    if (!toggle || !panel) return
    panel.hidden = false
    toggle.setAttribute('aria-expanded', 'true')
    toggle.classList.add('form-more-toggle--open')
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

    const isInstallment = tx.installmentsNumber > 1 && tx.installmentGroupId
    if (isInstallment && tx.installmentIndex !== 1) {
        navigate(`/pages/crud/Transaction.html?id=${tx.installmentGroupId}`)
        return
    }
    if (isInstallment) {
        const alertEl = document.getElementById('installment-edit-alert')
        document.getElementById('installment-edit-alert-text').textContent =
            I18n.t('installmentEditCascadeInfo', { count: tx.installmentsNumber })
        alertEl.hidden = false
    }

    setBreadcrumb([
        { i18nKey: 'movements', url: '/pages/HomePage.html' },
        { label: Transaction.formatLabel(tx), url: `/pages/views/TransactionView.html?id=${transactionId}` },
        { i18nKey: 'edit' }
    ])

    document.getElementById('debit-radio').checked  = tx.type === 'debit'
    document.getElementById('credit-radio').checked = tx.type === 'credit'
    updateRadioStyle()

    document.getElementById('date-input').value                        = tx.date
    document.getElementById('value-input').value                       = (isInstallment ? tx.installmentTotalValue : tx.value).toFixed(2)
    document.getElementById('installments-number-input').value         = tx.installmentsNumber
    document.getElementById('obs-input').value                         = tx.obs ?? ''
    document.getElementById('transfer-partner-id').value               = tx.transferPartnerId

    selectOptionByText('account-input',            tx.account)
    selectOptionByText('category-input',           tx.category)
    selectOptionByText('transaction-locale-input', tx.transactionLocale)

    if ((tx.installmentsNumber && tx.installmentsNumber > 0) || tx.transactionLocale || (tx.obs && tx.obs.trim()))
        openMoreOptions()

    const deleteBtn = addDeleteIcon()
    deleteBtn.addEventListener('click', () => {
        $.ajax({
            url:   `/api/transactions/${transactionId}`,
            type:  'DELETE',
            async: false,
            success: () => { clearDirtyGuard(); navigate('/pages/HomePage.html') },
            error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingTransaction'), 'error')
        })
    })
}

async function handleSave(transactionId, force = false) {
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
        showToast(I18n.t('commonFillRequired', { fields: emptyFields.join(', ') }), 'warning')
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

    const url    = transactionId ? `/api/transactions/${transactionId}` : `/api/transactions${force ? '?force=true' : ''}`
    const method = transactionId ? 'PUT' : 'POST'

    let result = null
    $.ajax({
        url, type: method, async: false, contentType: 'application/json',
        data: JSON.stringify(body),
        success: data => { result = { ok: true, data } },
        error:   xhr  => { result = { ok: false, xhr } }
    })

    if (!result.ok) {
        if (!transactionId && result.xhr.responseJSON?.errorCode === 'error.duplicate.transaction') {
            const proceed = await showConfirmAsync(
                I18n.t('duplicateTransactionConfirm'),
                null,
                { cancelLabel: I18n.t('commonCancel'), confirmLabel: I18n.t('createAnyway'), confirmClass: 'btn-primary' }
            )
            if (proceed) handleSave(transactionId, true)
            return
        }
        showToast(result.xhr.responseJSON?.message ?? I18n.t('errorSavingTransaction'), 'error')
        return
    }

    clearDirtyGuard()
    const notifications = (!transactionId && result.data?.notifications?.length) ? result.data.notifications : []
    if (notifications.length > 0) sessionStorage.setItem('pendingNotifications', JSON.stringify(notifications))
    const id = transactionId ?? result.data?.transaction?.id ?? result.data?.id
    if (transactionId) {
        const installments = Number(document.getElementById('installments-number-input').value) || 0
        if (installments > 1) {
            navigateWithToast(`/pages/views/TransactionView.html?id=${id}`, I18n.t('installmentEditCascadeDone'), 'success')
        } else {
            navigate(`/pages/views/TransactionView.html?id=${id}`)
        }
    } else {
        navigateWithToast('/pages/HomePage.html', I18n.t('transactionCreatedSuccess'), 'success', id ? `/pages/views/TransactionView.html?id=${id}` : null)
    }
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
                    title: I18n.t('newFinancialInstitution'), btnTitle: I18n.t('quickAdd', { item: I18n.t('financialInstitution') }), apiUrl: '/api/financial-institutions',
                    fields: [
                        { id: 'name',    label: `${I18n.t('institutionName')} *`, type: 'text', required: true, placeholder: I18n.t('institutionNamePlaceholder') },
                        { id: 'iconKey', label: I18n.t('categoryIcon'), type: 'icon-picker' }
                    ],
                    buildBody: v => ({ name: v.name, iconKey: v.iconKey || null })
                  }
                },
                { id: 'iconKey', label: I18n.t('categoryIcon'), type: 'icon-picker' }
            ],
            buildBody: v => ({
                name: v.name, financialInstitutionId: Number(v.fiId),
                balance: 0, contact: null, description: null, iconKey: v.iconKey || null
            }),
            onSuccess: item => addOptionToSelect('account-input', item.id, item.name)
        })
    })

    document.getElementById('category-add-btn').addEventListener('click', () => {
        showQuickAdd({
            title:  I18n.t('newCategory'),
            apiUrl: '/api/categories',
            fields: [
                { id: 'name',    label: `${I18n.t('categoryName')} *`, type: 'text', required: true, placeholder: I18n.t('categoryNamePlaceholder') },
                { id: 'iconKey', label: I18n.t('categoryIcon'), type: 'icon-picker' }
            ],
            buildBody: v => ({ name: v.name, iconKey: v.iconKey || null }),
            onSuccess: item => addOptionToSelect('category-input', item.id, item.name)
        })
    })

    document.getElementById('locale-add-btn').addEventListener('click', () => {
        showQuickAdd({
            title:  I18n.t('newLocale'),
            apiUrl: '/api/transaction-locales',
            fields: [
                { id: 'name',    label: `${I18n.t('localeName')} *`, type: 'text', required: true, placeholder: I18n.t('localeNamePlaceholder') },
                { id: 'iconKey', label: I18n.t('categoryIcon'), type: 'icon-picker' }
            ],
            buildBody: v => ({ name: v.name, iconKey: v.iconKey || null }),
            onSuccess: item => addOptionToSelect('transaction-locale-input', item.id, item.name)
        })
    })
}

if (!globalThis.__appRouter) init()
