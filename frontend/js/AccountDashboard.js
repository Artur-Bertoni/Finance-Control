import { doRequest, formatCurrency, navigate, showPendingToast } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { CustomSelect } from './components/CustomSelect.js'
import { Icons } from './icons/IconLibrary.js'
import { I18n } from './i18n.js'

let allAccounts = []
let allFinancialInstitutions = []
let searchQuery = ''
let selectedFinancialInstitutionId = ''

export function init() {
    document.body.classList.add('page-dashboard')
    SidebarManager.initialize()
    CustomSelect.autoInit()
    showPendingToast()
    loadData()
    setupSearch()
    setupFinancialInstitutionFilter()
    I18n.onChange(renderList)
}

function loadData() {
    try {
        const data = doRequest('/api/accounts', 'GET')
        allAccounts = (data ?? []).map(el => Account.processAccount(el))
    } catch (e) {
        console.log('Erro ao carregar contas:', e)
        allAccounts = []
    }
    
    try {
        const data = doRequest('/api/financial-institutions', 'GET')
        allFinancialInstitutions = data ?? []
        populateFinancialInstitutionFilter()
    } catch (e) {
        console.log('Erro ao carregar instituições financeiras:', e)
        allFinancialInstitutions = []
    }
    
    renderList()
}

function setupSearch() {
    const input = document.getElementById('search-input')
    const clearBtn = document.getElementById('clear-search-btn')
    if (clearBtn) clearBtn.innerHTML = Icons.broom()
    input?.addEventListener('input', () => {
        searchQuery = input.value
        renderList()
    })
    clearBtn?.addEventListener('click', () => {
        searchQuery = ''
        selectedFinancialInstitutionId = ''
        if (input) input.value = ''
        const fiFilter = document.getElementById('fi-filter')
        if (fiFilter) fiFilter.value = ''
        renderList()
    })
}

function populateFinancialInstitutionFilter() {
    const select = document.getElementById('fi-filter')
    if (!select) return
    
    for (const fi of allFinancialInstitutions) {
        const option = document.createElement('option')
        option.value = fi.id
        option.textContent = fi.name
        select.appendChild(option)
    }
}

function setupFinancialInstitutionFilter() {
    const select = document.getElementById('fi-filter')
    if (!select) return
    
    select.addEventListener('change', () => {
        selectedFinancialInstitutionId = select.value
        renderList()
    })
}

function renderList() {
    const list = document.getElementById('accounts-list')
    if (!list) return
    list.innerHTML = ''

    const q = searchQuery.trim().toLowerCase()
    let accounts = allAccounts

    // Aplicar filtro de busca por nome
    if (q) {
        accounts = accounts.filter(a => a.name.toLowerCase().includes(q))
    }

    // Aplicar filtro por instituição financeira
    if (selectedFinancialInstitutionId) {
        accounts = accounts.filter(a => a.financialInstitutionId === parseInt(selectedFinancialInstitutionId))
    }

    if (accounts.length === 0) {
        list.innerHTML = `
            <div class="empty-state" style="grid-column:1/-1">
                ${Icons.emptyBuilding()}
                <p>${I18n.t('noAccountsRegistered')}</p>
            </div>`
        return
    }

    for (const acc of accounts) {
        const card = document.createElement('div')
        card.className = 'item-card'
        card.addEventListener('click', () => navigate(`/pages/AccountView.html?id=${acc.id}`))

        let balClass = 'zero'
        if (acc.balance > 0) balClass = 'positive'
        else if (acc.balance < 0) balClass = 'negative'

        card.innerHTML = `
            <div class="item-card-header">
                <span class="item-card-name">${acc.name}</span>
                <span class="item-card-badge"></span>
            </div>
            <div class="item-card-meta">
                <div class="item-card-row">
                    ${Icons.institutions()}
                    ${acc.financialInstitution || I18n.t('noInstitutionSelected')}
                </div>
                ${acc.contact ? `<div class="item-card-row">${Icons.phone()}${acc.contact}</div>` : ''}
            </div>
            <div class="item-balance ${balClass}">
                ${acc.balance >= 0 ? '+ ' : '- '} $ ${formatCurrency(Math.abs(acc.balance))}
            </div>`

        list.appendChild(card)
    }
}

if (!globalThis.__appRouter) init()
