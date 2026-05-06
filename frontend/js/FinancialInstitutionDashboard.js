import { doRequest, navigate, showPendingToast } from '../utils/FrontendFunctions.js'
import { FinancialInstitution } from './class/FinancialInstitutionClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'

export function init() {
    SidebarManager.initialize()
    showPendingToast()

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
            ${Icons.emptyBuilding()}
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
                    ${fi.address ? `<div class="item-card-row">${Icons.locations()}${fi.address}</div>` : ''}
                    ${fi.contact ? `<div class="item-card-row">${Icons.phone()}${fi.contact}</div>` : ''}
                </div>`
            list.appendChild(card)
        }
    }
}

if (!globalThis.__appRouter) init()
