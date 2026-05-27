import { doRequest, navigate, setupSearch, showPendingToast, initFilterToggle, createAddCard } from '../../utils/FrontendFunctions.js'
import { Category } from '../class/CategoryClass.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { Icons } from '../icons/IconLibrary.js'
import { I18n } from '../i18n.js'
import { createEmptyState } from '../components/EmptyState.js'

let allCategories = []
let searchQuery = ''
let filterToggle = null

export function init() {
    document.body.classList.add('page-dashboard')
    SidebarManager.initialize()
    showPendingToast()
    filterToggle = initFilterToggle(() => !!searchQuery)
    loadData()
    setupSearch(
        q => { searchQuery = q; renderList(); filterToggle?.syncActive() },
        () => { searchQuery = ''; renderList(); filterToggle?.syncActive() }
    )
    I18n.onChange(renderList)
}

function loadData() {
    try {
        const data = doRequest('/api/categories', 'GET')
        allCategories = (data ?? []).map(el => Category.processCategory(el))
    } catch {
        allCategories = []
    }
    renderList()
}

function renderList() {
    const list = document.getElementById('categories-list')
    if (!list) return
    list.innerHTML = ''

    list.appendChild(createAddCard(I18n.t('newCategory'), '/pages/crud/Category.html'))

    const q = searchQuery.trim().toLowerCase()
    const categories = q ? allCategories.filter(c => c.name.toLowerCase().includes(q)) : allCategories

    if (categories.length === 0) {
        list.appendChild(createEmptyState(Icons.categories(), I18n.t(allCategories.length === 0 ? 'noCategoriesEmpty' : 'noCategoriesRegistered')))
        return
    }

    for (const cat of categories) {
        const card = document.createElement('div')
        card.className = 'item-card'
        card.addEventListener('click', () => navigate(`/pages/views/CategoryView.html?id=${cat.id}`))
        const description = cat.description
            ? `</br><div class="item-card-meta"><div class="item-card-row">${cat.description}</div></div>`
            : ''
        const aliasText = cat.aliases.length > 0 ? cat.aliases.join(', ') : ''
        const aliasMeta = aliasText
            ? `<div class="item-card-meta"><span class="item-card-row">${aliasText}</span></div>`
            : ''
        const iconHtml = cat.iconKey
            ? `<span style="font-size:18px;color:var(--primary);flex-shrink:0"><i class="ph ${cat.iconKey}"></i></span>`
            : ''
        card.innerHTML = `
            <div class="item-card-header">
                <span class="item-card-name-group">${iconHtml}<span class="item-card-name">${cat.name}</span></span>
                <span class="item-card-badge"></span>
            </div>
            ${aliasMeta}
            ${description}`
        list.appendChild(card)
    }
}

if (!globalThis.__appRouter) init()
