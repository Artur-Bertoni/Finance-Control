import { addDeleteIcon, clearDirtyGuard, doRequest, navigate, navigateWithToast, setBreadcrumb, setupDirtyGuard, showConfirmAsync, showToast } from '../../utils/FrontendFunctions.js'
import { TransactionLocale } from '../class/TransactionLocaleClass.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from '../utils/FieldValidation.js'
import { I18n } from '../i18n.js'
import { IconPicker } from '../components/IconPicker.js'

export function init() {
    SidebarManager.initialize()
    IconPicker.init(iconKey => IconPicker.setValue(iconKey))

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
            if (locale.iconKey) IconPicker.setValue(locale.iconKey)

            setBreadcrumb([
                { i18nKey: 'locations', url: '/pages/lists/TransactionLocaleList.html' },
                { label: locale.name, url: `/pages/views/TransactionLocaleView.html?id=${localeId}` },
                { i18nKey: 'edit' }
            ])

            const deleteBtn = addDeleteIcon()
            deleteBtn.addEventListener('click', function () {
                $.ajax({
                    url:   `/api/transaction-locales/${localeId}`,
                    type:  'DELETE',
                    async: false,
                    success: function () { clearDirtyGuard(); navigate('/pages/lists/TransactionLocaleList.html') },
                    error:   function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingLocale'), 'error') }
                })
            })
        }
    }

    document.getElementById('cancel-btn').addEventListener('click', () =>
        navigate(localeId ? `/pages/views/TransactionLocaleView.html?id=${localeId}` : '/pages/lists/TransactionLocaleList.html')
    )

    document.getElementById('save-btn').addEventListener('click', async function () {
        const fieldLabels = { 'name-input': I18n.t('localeName') }
        const emptyFields = validateRequiredFields(['name-input'], fieldLabels)

        if (emptyFields.length > 0) {
            showToast(I18n.t('fillRequiredFields', { fields: emptyFields.join(', ') }), 'warning')
            return
        }

        const body = {
            name:    document.getElementById('name-input').value,
            address: document.getElementById('address-input').value || null,
            iconKey: IconPicker.getValue() || null
        }

        const url    = localeId ? `/api/transaction-locales/${localeId}` : '/api/transaction-locales'
        const method = localeId ? 'PUT' : 'POST'

        let result = null
        $.ajax({
            url, type: method, async: false, contentType: 'application/json',
            data: JSON.stringify(body),
            success: data => { result = { ok: true, data } },
            error:   xhr  => { result = { ok: false, xhr } }
        })

        if (!result.ok) {
            if (!localeId && result.xhr.responseJSON?.errorCode === 'error.duplicate.name') {
                const proceed = await showConfirmAsync(
                    I18n.t('duplicateItemConfirm', { name: body.name }),
                    null,
                    { cancelLabel: I18n.t('cancel'), confirmLabel: I18n.t('createAnyway'), confirmClass: 'btn-primary' }
                )
                if (!proceed) return
                $.ajax({
                    url: url + '?force=true', type: method, async: false, contentType: 'application/json',
                    data: JSON.stringify(body),
                    success: data => { result = { ok: true, data } },
                    error:   xhr  => { result = { ok: false, xhr } }
                })
            }
            if (!result.ok) {
                showToast(result.xhr.responseJSON?.message ?? I18n.t('errorSavingLocale'), 'error')
                return
            }
        }

        clearDirtyGuard()
        const id = localeId ?? result.data?.id
        if (localeId) {
            navigate(`/pages/views/TransactionLocaleView.html?id=${id}`)
        } else {
            navigateWithToast('/pages/lists/TransactionLocaleList.html', I18n.t('localeCreatedSuccess'), 'success', id ? `/pages/views/TransactionLocaleView.html?id=${id}` : null)
        }
    })

    setupDirtyGuard()
}

if (!globalThis.__appRouter) init()
