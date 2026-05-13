import { doRequest, navigate, setBreadcrumb, showConfirm, showToast } from '../utils/FrontendFunctions.js'
import { Category } from './class/CategoryClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()

    const categoryId = new URLSearchParams(globalThis.location.search).get('id')
    if (!categoryId) { navigate('/pages/CategoryDashboard.html'); return }

    const response = doRequest(`/api/categories/${categoryId}`, 'GET')
    if (!response?.id) { navigate('/pages/CategoryDashboard.html'); return }

    const cat = Category.processCategory(response)

    setBreadcrumb([
        { i18nKey: 'categories', url: '/pages/CategoryDashboard.html' },
        { label: cat.name }
    ])

    document.getElementById('detail-name').textContent = cat.name

    const descEl = document.getElementById('detail-description')
    if (cat.description) {
        descEl.textContent = cat.description
    } else {
        descEl.innerHTML = `<span class="detail-empty">${I18n.t('notInformed')}</span>`
    }

    if (cat.internalName) {
        document.getElementById('detail-internal-name').textContent = cat.internalName
        document.getElementById('internal-name-row').style.display = ''
    }

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate(`/pages/Category.html?id=${categoryId}`)
    )

    document.getElementById('delete-btn').addEventListener('click', () => {
        showConfirm(I18n.t('deleteConfirm'), () => {
            $.ajax({
                url:   `/api/categories/${categoryId}`,
                type:  'DELETE',
                async: false,
                success: () => navigate('/pages/CategoryDashboard.html'),
                error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingCategory'), 'error')
            })
        })
    })
}

if (!globalThis.__appRouter) init()
