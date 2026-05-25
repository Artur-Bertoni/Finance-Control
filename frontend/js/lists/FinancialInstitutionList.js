import { doRequest, navigate, setupSearch, showPendingToast, initFilterToggle, createAddCard } from '../../utils/FrontendFunctions.js'
import { FinancialInstitution } from '../class/FinancialInstitutionClass.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { Icons } from '../icons/IconLibrary.js'
import { I18n } from '../i18n.js'

let allInstitutions = []
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
        const data = doRequest('/api/financial-institutions', 'GET')
        allInstitutions = (data ?? []).map(el => FinancialInstitution.processFinancialInstitution(el))
    } catch {
        allInstitutions = []
    }
    renderList()
}

function renderList() {
    const list = document.getElementById('financial-institutions-list')
    if (!list) return
    list.innerHTML = ''

    list.appendChild(createAddCard(I18n.t('newInstitution'), '/pages/crud/FinancialInstitution.html'))

    const q = searchQuery.trim().toLowerCase()
    const institutions = q ? allInstitutions.filter(fi => fi.name.toLowerCase().includes(q)) : allInstitutions

    if (institutions.length === 0) {
        const empty = document.createElement('div')
        empty.className = 'empty-state'
        empty.style.gridColumn = '1 / -1'
        empty.innerHTML = `${Icons.institutions()}<p>${I18n.t(allInstitutions.length === 0 ? 'noInstitutionsEmpty' : 'noInstitutionsRegistered')}</p>`
        list.appendChild(empty)
        return
    }

    for (const fi of institutions) {
        const card = document.createElement('div')
        card.className = 'item-card'
        card.addEventListener('click', () => navigate(`/pages/views/FinancialInstitutionView.html?id=${fi.id}`))
        const iconHtml = fi.iconKey
            ? `<span style="font-size:18px;color:var(--primary);flex-shrink:0"><i class="ph ${fi.iconKey}"></i></span>`
            : ''
        card.innerHTML = `
            <div class="item-card-header">
                <span class="item-card-name-group">${iconHtml}<span class="item-card-name">${fi.name}</span></span>
                <span class="item-card-badge"></span>
            </div>
            <div class="item-card-meta">
                ${fi.address ? `<div class="item-card-row">${Icons.locations()}${fi.address}</div>` : ''}
                ${fi.contact ? `<div class="item-card-row">${Icons.phone()}${fi.contact}</div>` : ''}
            </div>`
        list.appendChild(card)
    }
}

if (!globalThis.__appRouter) init()
