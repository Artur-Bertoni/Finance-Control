import { doRequest, navigate } from '../utils/FrontendFunctions.js'
import { Category } from './class/CategoryClass.js'
import { SidebarManager } from './components/SidebarManager.js'

SidebarManager.initialize()

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
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path fill-rule="evenodd" d="M5.25 2.25a3 3 0 00-3 3v4.318a3 3 0 00.879 2.121l9.58 9.581c.92.92 2.39 1.186 3.548.428a18.849 18.849 0 005.441-5.44c.758-1.16.492-2.629-.428-3.548l-9.58-9.581a3 3 0 00-2.121-.879H5.25zM6.375 7.5a1.125 1.125 0 100-2.25 1.125 1.125 0 000 2.25z" clip-rule="evenodd"/></svg>
        <p>Nenhuma categoria cadastrada</p></div>`
} else {
    for (const cat of categories) {
        const card = document.createElement('div')
        card.className = 'item-card'
        card.addEventListener('click', () => navigate(`/pages/Category.html?id=${cat.id}`))
        card.innerHTML = `
            <div class="item-card-header">
                <span class="item-card-name">${cat.name}</span>
                <span class="item-card-badge"></span>
            </div>
            ${cat.description ? `<div class="item-card-meta"><div class="item-card-row">${cat.description}</div></div>` : ''}`
        list.appendChild(card)
    }
}
