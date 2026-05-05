import { doRequest, navigate } from '../utils/FrontendFunctions.js'
import { FinancialInstitution } from './class/FinancialInstitutionClass.js'
import { SidebarManager } from './components/SidebarManager.js'

SidebarManager.initialize()

const list = document.getElementById('financial-institutions-list')
list.innerHTML = ''

let institutions = []
try {
    const data = doRequest('/api/financial-institutions', 'GET')
    for (const el of (data ?? [])) institutions.push(FinancialInstitution.processFinancialInstitution(el))
} catch (e) {
    console.log('Erro ao carregar instituições:', e)
}

if (institutions.length === 0) {
    list.innerHTML = `<div class="empty-state" style="grid-column:1/-1">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h8a2 2 0 012 2v12a1 1 0 110 2h-3a1 1 0 01-1-1v-2a1 1 0 00-1-1H9a1 1 0 00-1 1v2a1 1 0 01-1 1H4a1 1 0 110-2V4z" clip-rule="evenodd"/></svg>
        <p>Nenhuma instituição financeira cadastrada</p></div>`
} else {
    for (const fi of institutions) {
        const card = document.createElement('div')
        card.className = 'item-card'
        card.addEventListener('click', () => navigate(`/pages/FinancialInstitution.html?id=${fi.id}`))
        card.innerHTML = `
            <div class="item-card-header">
                <span class="item-card-name">${fi.name}</span>
                <span class="item-card-badge"></span>
            </div>
            <div class="item-card-meta">
                ${fi.address ? `<div class="item-card-row"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/></svg>${fi.address}</div>` : ''}
                ${fi.contact ? `<div class="item-card-row"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"/></svg>${fi.contact}</div>` : ''}
            </div>`
        list.appendChild(card)
    }
}
