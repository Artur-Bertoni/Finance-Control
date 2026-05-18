import { doRequest, navigate, setBreadcrumb } from '../utils/FrontendFunctions.js'
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

    if (cat.iconKey) {
        const iconRow = document.getElementById('icon-row')
        const iconEl  = document.getElementById('detail-icon')
        if (iconRow) iconRow.style.display = ''
        if (iconEl)  iconEl.innerHTML = `<i class="ph ${cat.iconKey}"></i>`
    }

    document.getElementById('detail-name').textContent = cat.name

    const descEl = document.getElementById('detail-description')
    if (cat.description) {
        descEl.textContent = cat.description
    } else {
        descEl.innerHTML = `<span class="detail-empty">${I18n.t('notInformed')}</span>`
    }

    if (cat.aliases.length > 0) {
        const aliasesRow = document.getElementById('aliases-row')
        const aliasesEl  = document.getElementById('detail-aliases')
        aliasesRow.style.display = ''
        cat.aliases.forEach(a => {
            const tag = document.createElement('span')
            tag.style.cssText = 'background:var(--surface-raised);border:1px solid var(--border);border-radius:4px;padding:2px 8px;font-size:13px;font-family:monospace;color:var(--text-muted)'
            tag.textContent = a
            aliasesEl.appendChild(tag)
        })
    }

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate(`/pages/Category.html?id=${categoryId}`)
    )

    document.getElementById('view-transactions-btn')?.addEventListener('click', () => {
        const today = new Date()
        const firstOfYear = new Date(today.getFullYear(), 0, 1)
        const toStr = d => new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().split('T')[0]
        sessionStorage.setItem('__homeFilters', JSON.stringify({
            category:  categoryId,
            startDate: toStr(firstOfYear),
            endDate:   toStr(today),
        }))
        navigate('/pages/HomePage.html')
    })
}

if (!globalThis.__appRouter) init()
