import { addDeleteIcon, clearDirtyGuard, doRequest, navigate, navigateWithToast, setBreadcrumb, setupDirtyGuard, showToast } from '../utils/FrontendFunctions.js'
import { Category } from './class/CategoryClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()

    setupRequiredFieldValidation(['name-input'])

    const urlParams  = new URLSearchParams(globalThis.location.search)
    const categoryId = urlParams.get('id')

    if (categoryId) {
        const saveBtn = document.getElementById('save-btn')
        if (saveBtn) {
            saveBtn.dataset.i18n = 'saveChanges'
            saveBtn.textContent  = I18n.t('saveChanges')
        }

        const response = doRequest(`/api/categories/${categoryId}`, 'GET')
        if (response?.id !== undefined) {
            const cat = Category.processCategory(response)
            document.getElementById('name-input').value        = cat.name ?? ''
            document.getElementById('description-input').value = cat.description ?? ''

            setBreadcrumb([
                { i18nKey: 'categories', url: '/pages/CategoryDashboard.html' },
                { label: cat.name, url: `/pages/CategoryView.html?id=${categoryId}` },
                { i18nKey: 'edit' }
            ])

            const deleteBtn = addDeleteIcon()
            deleteBtn.addEventListener('click', function () {
                $.ajax({
                    url:   `/api/categories/${categoryId}`,
                    type:  'DELETE',
                    async: false,
                    success: function () { clearDirtyGuard(); navigate('/pages/CategoryDashboard.html') },
                    error:   function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingCategory'), 'error') }
                })
            })
        }
    }

    document.getElementById('cancel-btn').addEventListener('click', () =>
        navigate(categoryId ? `/pages/CategoryView.html?id=${categoryId}` : '/pages/CategoryDashboard.html')
    )

    document.getElementById('save-btn').addEventListener('click', function () {
        const fieldLabels = { 'name-input': I18n.t('categoryName') }
        const emptyFields = validateRequiredFields(['name-input'], fieldLabels)

        if (emptyFields.length > 0) {
            showToast(I18n.t('fillRequiredFields', { fields: emptyFields.join(', ') }), 'warning')
            return
        }

        const body = {
            name:        document.getElementById('name-input').value,
            description: document.getElementById('description-input').value || null
        }

        $.ajax({
            url:         categoryId ? `/api/categories/${categoryId}` : '/api/categories',
            type:        categoryId ? 'PUT' : 'POST',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify(body),
            success:     function () {
                clearDirtyGuard()
                const msg = categoryId ? I18n.t('categoryUpdatedSuccess') : I18n.t('categoryCreatedSuccess')
                navigateWithToast('/pages/CategoryDashboard.html', msg, 'success')
            },
            error:       function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingCategory'), 'error') }
        })
    })

    setupDirtyGuard()
}

if (!globalThis.__appRouter) init()
