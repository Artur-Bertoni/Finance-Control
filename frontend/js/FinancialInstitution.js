import { addDeleteIcon, doRequest, navigate, navigateWithToast, showToast } from '../utils/FrontendFunctions.js'
import { FinancialInstitution } from './class/FinancialInstitutionClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()

    setupRequiredFieldValidation(['name-input'])

    const urlParams = new URLSearchParams(globalThis.location.search)
    const fiId      = urlParams.get('id')

    if (fiId) {
        const titleEl = document.getElementById('page-title-text')
        if (titleEl) {
            titleEl.dataset.i18n = 'editFinancialInstitution'
            titleEl.textContent  = I18n.t('editFinancialInstitution')
        }
        const saveBtn = document.getElementById('save-btn')
        if (saveBtn) {
            saveBtn.dataset.i18n = 'saveChanges'
            saveBtn.textContent  = I18n.t('saveChanges')
        }

        const response = doRequest(`/api/financial-institutions/${fiId}`, 'GET')
        if (response?.id !== undefined) {
            const fi = FinancialInstitution.processFinancialInstitution(response)
            document.getElementById('name-input').value    = fi.name    ?? ''
            document.getElementById('address-input').value = fi.address ?? ''
            document.getElementById('contact-input').value = fi.contact ?? ''

            const deleteBtn = addDeleteIcon()
            deleteBtn.addEventListener('click', function () {
                $.ajax({
                    url:   `/api/financial-institutions/${fiId}`,
                    type:  'DELETE',
                    async: false,
                    success: function () { navigate('/pages/FinancialInstitutionDashboard.html') },
                    error:   function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingInstitution'), 'error') }
                })
            })
        }
    }

    document.getElementById('cancel-btn').addEventListener('click', () =>
        navigate('/pages/FinancialInstitutionDashboard.html')
    )

    document.getElementById('save-btn').addEventListener('click', function () {
        const fieldLabels = { 'name-input': I18n.t('institutionName') }
        const emptyFields = validateRequiredFields(['name-input'], fieldLabels)

        if (emptyFields.length > 0) {
            showToast(I18n.t('fillRequiredFields', { fields: emptyFields.join(', ') }), 'warning')
            return
        }

        const body = {
            name:    document.getElementById('name-input').value,
            address: document.getElementById('address-input').value || null,
            contact: document.getElementById('contact-input').value || null
        }

        $.ajax({
            url:         fiId ? `/api/financial-institutions/${fiId}` : '/api/financial-institutions',
            type:        fiId ? 'PUT' : 'POST',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify(body),
            success:     function () {
                const msg = fiId ? I18n.t('institutionUpdatedSuccess') : I18n.t('institutionCreatedSuccess')
                navigateWithToast('/pages/FinancialInstitutionDashboard.html', msg, 'success')
            },
            error:       function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingInstitution'), 'error') }
        })
    })
}

if (!globalThis.__appRouter) init()
