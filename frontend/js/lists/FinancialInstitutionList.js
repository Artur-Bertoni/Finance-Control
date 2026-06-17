import { doRequest, navigate, setupSearch, showPendingToast, initFilterToggle, createAddCard } from '../../utils/FrontendFunctions.js'
import { FinancialInstitution } from '../class/FinancialInstitutionClass.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { Icons } from '../icons/IconLibrary.js'
import { I18n } from '../i18n.js'
import { createEmptyState } from '../components/EmptyState.js'

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
        list.appendChild(createEmptyState(Icons.institutions(), I18n.t(allInstitutions.length === 0 ? 'noInstitutionsEmpty' : 'noInstitutionsRegistered')))
        return
    }

    for (const fi of institutions) list.appendChild(_buildFiCard(fi))
}

function _buildFiCard(fi) {
    const card = document.getElementById('tpl-fi-card').content.firstElementChild.cloneNode(true)
    card.addEventListener('click', () => navigate(`/pages/views/FinancialInstitutionView.html?id=${fi.id}`))

    if (fi.iconKey) {
        const iconEl = card.querySelector('.fi-icon')
        iconEl.innerHTML = `<i class="ph ${fi.iconKey}"></i>`
        iconEl.hidden = false
    }
    card.querySelector('.item-card-name').textContent = fi.name

    if (fi.address) {
        const row = card.querySelector('.fi-address')
        row.innerHTML = Icons.locations()
        row.appendChild(document.createTextNode(fi.address))
        row.hidden = false
    }
    if (fi.contact) {
        const row = card.querySelector('.fi-contact')
        row.innerHTML = Icons.phone()
        row.appendChild(document.createTextNode(fi.contact))
        row.hidden = false
    }
    return card
}

if (!globalThis.__appRouter) init()
