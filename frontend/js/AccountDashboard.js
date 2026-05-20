import { doRequest, formatCurrency, navigate, setupSearch, showPendingToast } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { CustomSelect } from './components/CustomSelect.js'
import { Icons } from './icons/IconLibrary.js'
import { I18n } from './i18n.js'

let allAccounts = []
let allFinancialInstitutions = []
let searchQuery = ''
let selectedFinancialInstitutionId = ''

function syncClearBtn() {
    const btn = document.getElementById('clear-search-btn')
    const wrapper = btn?.closest('.filter-clear-field') ?? btn
    if (!wrapper) return
    wrapper.style.display = (searchQuery || selectedFinancialInstitutionId) ? 'flex' : 'none'
}

export function init() {
    document.body.classList.add('page-dashboard')
    SidebarManager.initialize()
    CustomSelect.autoInit()
    showPendingToast()
    loadData()
    setupSearch(
        q => { searchQuery = q; renderList() },
        () => {
            searchQuery = ''
            selectedFinancialInstitutionId = ''
            const fiFilter = document.getElementById('fi-filter')
            if (fiFilter) fiFilter.value = ''
            renderList()
        },
        syncClearBtn
    )
    setupFinancialInstitutionFilter()
    I18n.onChange(renderList)
}

function loadData() {
    try {
        const data = doRequest('/api/accounts', 'GET')
        allAccounts = (data ?? []).map(el => Account.processAccount(el))
    } catch {
        allAccounts = []
    }

    try {
        const data = doRequest('/api/financial-institutions', 'GET')
        allFinancialInstitutions = data ?? []
        populateFinancialInstitutionFilter()
    } catch {
        allFinancialInstitutions = []
    }

    renderList()
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
        syncClearBtn()
        renderList()
    })
}

function renderList() {
    const list = document.getElementById('accounts-list')
    if (!list) return
    list.innerHTML = ''

    const q = searchQuery.trim().toLowerCase()
    let accounts = allAccounts

    if (q) {
        accounts = accounts.filter(a => a.name.toLowerCase().includes(q))
    }

    if (selectedFinancialInstitutionId) {
        accounts = accounts.filter(a => a.financialInstitutionId === Number.parseInt(selectedFinancialInstitutionId))
    }

    if (accounts.length === 0) {
        const empty = document.createElement('div')
        empty.className = 'empty-state'
        empty.style.gridColumn = '1 / -1'
        if (allAccounts.length === 0) {
            empty.innerHTML = `${Icons.accounts()}<p>${I18n.t('noAccountsEmpty')}</p>`
            const btn = document.createElement('button')
            btn.className = 'btn btn-primary btn-sm'
            btn.textContent = I18n.t('newAccount')
            btn.addEventListener('click', () => navigate('/pages/Account.html'))
            empty.appendChild(btn)
        } else {
            empty.innerHTML = `${Icons.accounts()}<p>${I18n.t('noAccountsRegistered')}</p>`
        }
        list.appendChild(empty)
        return
    }

    for (const acc of accounts) {
        const card = document.createElement('div')
        card.className = 'item-card'
        card.addEventListener('click', () => navigate(`/pages/AccountView.html?id=${acc.id}`))

        let balClass = 'zero'
        if (acc.balance > 0) balClass = 'positive'
        else if (acc.balance < 0) balClass = 'negative'

        const iconHtml = acc.iconKey
            ? `<span style="font-size:18px;color:var(--primary);flex-shrink:0"><i class="ph ${acc.iconKey}"></i></span>`
            : ''

        card.innerHTML = `
            <div class="item-card-header">
                <span class="item-card-name-group">${iconHtml}<span class="item-card-name">${acc.name}</span></span>
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
