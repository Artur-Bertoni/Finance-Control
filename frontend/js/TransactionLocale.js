import { addDeleteIcon, clearDirtyGuard, doRequest, navigate, navigateWithToast, setBreadcrumb, setupDirtyGuard, showToast } from '../utils/FrontendFunctions.js'
import { TransactionLocale } from './class/TransactionLocaleClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()

    setupRequiredFieldValidation(['name-input'])

    const urlParams = new URLSearchParams(globalThis.location.search)
    const localeId  = urlParams.get('id')

    if (localeId) {
        const saveBtn = document.getElementById('save-btn')
        if (saveBtn) {
            saveBtn.dataset.i18n = 'saveChanges'
            saveBtn.textContent  = I18n.t('saveChanges')
        }

        const response = doRequest(`/api/transaction-locales/${localeId}`, 'GET')
        if (response?.id !== undefined) {
            const locale = TransactionLocale.processTransactionLocale(response)
            document.getElementById('name-input').value    = locale.name    ?? ''
            document.getElementById('address-input').value = locale.address ?? ''

            setBreadcrumb([
                { i18nKey: 'locations', url: '/pages/TransactionLocaleDashboard.html' },
                { label: locale.name, url: `/pages/TransactionLocaleView.html?id=${localeId}` },
                { i18nKey: 'edit' }
            ])

            const deleteBtn = addDeleteIcon()
            deleteBtn.addEventListener('click', function () {
                $.ajax({
                    url:   `/api/transaction-locales/${localeId}`,
                    type:  'DELETE',
                    async: false,
                    success: function () { clearDirtyGuard(); navigate('/pages/TransactionLocaleDashboard.html') },
                    error:   function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingLocale'), 'error') }
                })
            })
        }
    }

    document.getElementById('cancel-btn').addEventListener('click', () =>
        navigate(localeId ? `/pages/TransactionLocaleView.html?id=${localeId}` : '/pages/TransactionLocaleDashboard.html')
    )

    document.getElementById('save-btn').addEventListener('click', function () {
        const fieldLabels = { 'name-input': I18n.t('localeName') }
        const emptyFields = validateRequiredFields(['name-input'], fieldLabels)

        if (emptyFields.length > 0) {
            showToast(I18n.t('fillRequiredFields', { fields: emptyFields.join(', ') }), 'warning')
            return
        }

        const body = {
            name:    document.getElementById('name-input').value,
            address: document.getElementById('address-input').value || null
        }

        $.ajax({
            url:         localeId ? `/api/transaction-locales/${localeId}` : '/api/transaction-locales',
            type:        localeId ? 'PUT' : 'POST',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify(body),
            success:     function () {
                clearDirtyGuard()
                const msg = localeId ? I18n.t('localeUpdatedSuccess') : I18n.t('localeCreatedSuccess')
                navigateWithToast('/pages/TransactionLocaleDashboard.html', msg, 'success')
            },
            error:       function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingLocale'), 'error') }
        })
    })

    setupDirtyGuard()
}

if (!globalThis.__appRouter) init()
