import { doRequest, navigate, setupSearch, showPendingToast } from '../utils/FrontendFunctions.js'
import { Category } from './class/CategoryClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'
import { I18n } from './i18n.js'

let allCategories = []
let searchQuery = ''

export function init() {
    document.body.classList.add('page-dashboard')
    SidebarManager.initialize()
    showPendingToast()
    loadData()
    setupSearch(q => { searchQuery = q; renderList() }, () => { searchQuery = ''; renderList() })
    I18n.onChange(renderList)
}

function loadData() {
    try {
        const data = doRequest('/api/categories', 'GET')
        allCategories = (data ?? []).map(el => Category.processCategory(el))
    } catch (e) {
        console.error('Erro ao carregar categorias:', e)
        allCategories = []
    }
    renderList()
}

function renderList() {
    const list = document.getElementById('categories-list')
    if (!list) return
    list.innerHTML = ''

    const q = searchQuery.trim().toLowerCase()
    const categories = q ? allCategories.filter(c => c.name.toLowerCase().includes(q)) : allCategories

    if (categories.length === 0) {
        list.innerHTML = `
            <div class="empty-state" style="grid-column:1/-1">
                ${Icons.emptyCategory()}
                <p>${I18n.t('noCategoriesRegistered')}</p>
            </div>`
        return
    }

    for (const cat of categories) {
        const card = document.createElement('div')
        card.className = 'item-card'
        card.addEventListener('click', () => navigate(`/pages/CategoryView.html?id=${cat.id}`))
        const metaHtml = cat.description
            ? `<div class="item-card-meta"><div class="item-card-row">${cat.description}</div></div>`
            : ''
        card.innerHTML = `
            <div class="item-card-header">
                <span class="item-card-name">${cat.name}</span>
                <span class="item-card-badge"></span>
            </div>
            ${metaHtml}`
        list.appendChild(card)
    }
}

if (!globalThis.__appRouter) init()
