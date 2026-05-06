import { addDeleteIcon, doRequest, navigate, navigateWithToast, showToast } from '../utils/FrontendFunctions.js'
import { Category } from './class/CategoryClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'

export function init() {
    SidebarManager.initialize()

    setupRequiredFieldValidation(['name-input'])

    const urlParams  = new URLSearchParams(globalThis.location.search)
    const categoryId = urlParams.get('id')

    if (categoryId) {
        document.getElementById('page-title-text').textContent = 'Editar Categoria'
        document.getElementById('save-btn').textContent = 'Salvar Alterações'

        const response = doRequest(`/api/categories/${categoryId}`, 'GET')
        if (response?.id !== undefined) {
            const cat = Category.processCategory(response)
            document.getElementById('name-input').value        = cat.name ?? ''
            document.getElementById('description-input').value = cat.description ?? ''

            const deleteBtn = addDeleteIcon()
            deleteBtn.addEventListener('click', function () {
                $.ajax({
                    url:   `/api/categories/${categoryId}`,
                    type:  'DELETE',
                    async: false,
                    success: function () { navigate('/pages/CategoryDashboard.html') },
                    error:   function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao excluir categoria.', 'error') }
                })
            })
        }
    }

    document.getElementById('cancel-btn').addEventListener('click', () =>
        navigate('/pages/CategoryDashboard.html')
    )

    document.getElementById('save-btn').addEventListener('click', function () {
        const name = document.getElementById('name-input').value

        const fieldLabels = { 'name-input': 'Nome' }

        const emptyFields = validateRequiredFields(['name-input'], fieldLabels)

        if (emptyFields.length > 0) {
            showToast(`Preencha os campos obrigatórios: ${emptyFields.join(', ')}.`, 'warning')
            return
        }

        const body = {
            name,
            description: document.getElementById('description-input').value || null
        }

        $.ajax({
            url:         categoryId ? `/api/categories/${categoryId}` : '/api/categories',
            type:        categoryId ? 'PUT' : 'POST',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify(body),
            success:     function () {
                const msg = categoryId ? 'Categoria atualizada com sucesso!' : 'Categoria criada com sucesso!'
                navigateWithToast('/pages/CategoryDashboard.html', msg, 'success')
            },
            error:       function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao salvar categoria.', 'error') }
        })
    })
}

if (!globalThis.__appRouter) init()
