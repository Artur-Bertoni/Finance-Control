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

    for (const cat of categories) list.appendChild(_buildCategoryCard(cat))
}

function _buildCategoryCard(cat) {
    const card = document.getElementById('tpl-category-card').content.firstElementChild.cloneNode(true)
    card.addEventListener('click', () => navigate(`/pages/views/CategoryView.html?id=${cat.id}`))

    if (cat.iconKey) {
        const iconEl = card.querySelector('.cat-icon')
        iconEl.innerHTML = `<i class="ph ${cat.iconKey}"></i>`
        iconEl.hidden = false
    }
    card.querySelector('.item-card-name').textContent = cat.name

    const aliasText = cat.aliases.length > 0 ? cat.aliases.join(', ') : ''
    if (aliasText) {
        const a = card.querySelector('.cat-aliases')
        a.querySelector('.item-card-row').textContent = aliasText
        a.hidden = false
    }
    if (cat.description) {
        const d = card.querySelector('.cat-desc')
        d.querySelector('.item-card-row').textContent = cat.description
        d.hidden = false
    }
    return card
}

if (!globalThis.__appRouter) init()
