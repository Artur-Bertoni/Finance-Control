import { addDeleteIcon, clearDirtyGuard, doRequest, navigate, navigateWithToast, setBreadcrumb, setupDirtyGuard, showConfirm, showToast } from '../utils/FrontendFunctions.js'
import { Category } from './class/CategoryClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'
import { IconPicker } from './components/IconPicker.js'
import { I18n } from './i18n.js'

let aliases = []

export function init() {
    aliases = []
    SidebarManager.initialize()
    setupRequiredFieldValidation(['name-input'])
    IconPicker.init(key => { IconPicker.setValue(key); setupDirtyGuard() })

    const urlParams  = new URLSearchParams(globalThis.location.search)
    const categoryId = urlParams.get('id')
    const nameInput  = document.getElementById('name-input')

    let originalAliases = null

    if (categoryId) {
        const saveBtn = document.getElementById('save-btn')
        if (saveBtn) {
            saveBtn.dataset.i18n = 'saveChanges'
            saveBtn.textContent  = I18n.t('saveChanges')
        }

        const response = doRequest(`/api/categories/${categoryId}`, 'GET')
        if (response?.id !== undefined) {
            const cat = Category.processCategory(response)
            nameInput.value = cat.name ?? ''
            document.getElementById('description-input').value = cat.description ?? ''
            if (cat.iconKey) IconPicker.setValue(cat.iconKey)
            aliases = [...cat.aliases]
            originalAliases = [...cat.aliases]
            renderAliases()

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
    } else {
        // On create: sync first alias with the name field until the user edits aliases manually
        aliases = ['']
        renderAliases()

        nameInput.addEventListener('input', () => {
            if (aliases.length === 1 && !aliasManuallyEdited) {
                aliases[0] = nameInput.value
                renderAliases()
            }
        })
    }

    I18n.onChange(() => renderAliases())

    document.getElementById('add-alias-btn').addEventListener('click', () => {
        aliases.push('')
        renderAliases()
        const inputs = document.querySelectorAll('.alias-input')
        inputs[inputs.length - 1]?.focus()
    })

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

        const currentAliases = aliases.filter(a => a.trim() !== '')
        const aliasesChanged = categoryId && originalAliases !== null &&
            JSON.stringify(currentAliases) !== JSON.stringify(originalAliases)

        function doSave() {
            const body = {
                name:        nameInput.value,
                description: document.getElementById('description-input').value || null,
                iconKey:     IconPicker.getValue(),
                aliases:     currentAliases.length > 0 ? currentAliases : [nameInput.value]
            }

            $.ajax({
                url:         categoryId ? `/api/categories/${categoryId}` : '/api/categories',
                type:        categoryId ? 'PUT' : 'POST',
                async:       false,
                contentType: 'application/json',
                data:        JSON.stringify(body),
                success:     function (data) {
                    clearDirtyGuard()
                    const id = categoryId ?? data?.id
                    if (categoryId) {
                        navigate(`/pages/CategoryView.html?id=${id}`)
                    } else {
                        navigateWithToast('/pages/CategoryDashboard.html', I18n.t('categoryCreatedSuccess'), 'success', id ? `/pages/CategoryView.html?id=${id}` : null)
                    }
                },
                error:       function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingCategory'), 'error') }
            })
        }

        if (aliasesChanged) {
            showConfirm(I18n.t('aliasChangeWarning'), doSave, I18n.t('confirmAction'))
        } else {
            doSave()
        }
    })

    setupDirtyGuard()
}

let aliasManuallyEdited = false

function syncAddButton() {
    const btn = document.getElementById('add-alias-btn')
    if (btn) btn.disabled = aliases.some(a => a.trim() === '')
}

function renderAliases() {
    const container = document.getElementById('aliases-list')
    if (!container) return
    container.innerHTML = ''

    aliases.forEach((alias, index) => {
        const row = document.createElement('div')
        row.className = 'alias-row'
        row.style.cssText = 'display:flex;gap:8px;margin-bottom:6px;align-items:center'

        const input = document.createElement('input')
        input.type = 'text'
        input.className = 'alias-input'
        input.value = alias
        input.style.flex = '1'
        input.addEventListener('input', () => {
            aliases[index] = input.value
            aliasManuallyEdited = true
            syncAddButton()
        })

        const removeBtn = document.createElement('button')
        removeBtn.type = 'button'
        removeBtn.className = 'btn btn-ghost btn-sm'
        removeBtn.style.cssText = 'padding:4px 8px;color:var(--text-muted);flex-shrink:0'
        removeBtn.textContent = '×'
        removeBtn.title = I18n.t('remove')
        removeBtn.addEventListener('click', () => {
            aliases.splice(index, 1)
            aliasManuallyEdited = true
            renderAliases()
        })

        row.appendChild(input)
        row.appendChild(removeBtn)
        container.appendChild(row)
    })

    syncAddButton()
}

if (!globalThis.__appRouter) init()
