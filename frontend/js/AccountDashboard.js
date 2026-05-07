import { doRequest, formatCurrency, navigate, showPendingToast } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'
import { I18n } from './i18n.js'

let allAccounts = []
let searchQuery = ''

export function init() {
    document.body.classList.add('page-dashboard')
    SidebarManager.initialize()
    showPendingToast()
    loadData()
    setupSearch()
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
        if (input) input.value = ''
        renderList()
    })
}

function renderList() {
    const list = document.getElementById('accounts-list')
    if (!list) return
    list.innerHTML = ''

    const q = searchQuery.trim().toLowerCase()
    const accounts = q ? allAccounts.filter(a => a.name.toLowerCase().includes(q)) : allAccounts

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
