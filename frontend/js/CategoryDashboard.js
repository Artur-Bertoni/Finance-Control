import { doRequest, navigate, showPendingToast } from '../utils/FrontendFunctions.js'
import { Category } from './class/CategoryClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'

export function init() {
    SidebarManager.initialize()
    showPendingToast()

    const list = document.getElementById('categories-list')
    list.innerHTML = ''

    let categories = []
    try {
        const data = doRequest('/api/categories', 'GET')
        for (const el of (data ?? [])) categories.push(Category.processCategory(el))
    } catch (e) {
        console.log('Erro ao carregar categorias:', e)
    }

    if (categories.length === 0) {
        list.innerHTML = `<div class="empty-state" style="grid-column:1/-1">
            ${Icons.emptyCategory()}
            <p>Nenhuma categoria cadastrada</p></div>`
    } else {
        for (const cat of categories) {
            const card = document.createElement('div')
            card.className = 'item-card'
            card.addEventListener('click', () => navigate(`/pages/Category.html?id=${cat.id}`))
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
}

if (!globalThis.__appRouter) init()
