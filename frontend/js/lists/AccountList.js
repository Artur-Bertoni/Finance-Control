import { doRequest, formatCurrency, navigate, setupSearch, showPendingToast, initFilterToggle, createAddCard } from '../../utils/FrontendFunctions.js'
import { Account } from '../class/AccountClass.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { CustomSelect } from '../components/CustomSelect.js'
import { Icons } from '../icons/IconLibrary.js'
import { I18n } from '../i18n.js'
import { createEmptyState } from '../components/EmptyState.js'

let allAccounts = []
let allFinancialInstitutions = []
let searchQuery = ''
let selectedFinancialInstitutionId = ''
let filterToggle = null

function isFilterActive() {
    return !!(searchQuery || selectedFinancialInstitutionId)
}

function syncClearBtn() {
    const btn = document.getElementById('clear-search-btn')
    const wrapper = btn?.closest('.filter-clear-field') ?? btn
    if (!wrapper) return
    wrapper.style.display = isFilterActive() ? 'flex' : 'none'
    filterToggle?.syncActive()
}

export function init() {
    document.body.classList.add('page-dashboard')
    SidebarManager.initialize()
    CustomSelect.autoInit()
    showPendingToast()
    filterToggle = initFilterToggle(isFilterActive)
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

    list.appendChild(createAddCard(I18n.t('newAccount'), '/pages/crud/Account.html'))

    const q = searchQuery.trim().toLowerCase()
    let accounts = allAccounts
    if (q) accounts = accounts.filter(a => a.name.toLowerCase().includes(q))
    if (selectedFinancialInstitutionId) accounts = accounts.filter(a => a.financialInstitutionId === Number.parseInt(selectedFinancialInstitutionId))

    if (accounts.length === 0) {
        list.appendChild(createEmptyState(Icons.accounts(), I18n.t(allAccounts.length === 0 ? 'noAccountsEmpty' : 'noAccountsRegistered')))
        return
    }

    for (const acc of accounts) list.appendChild(_buildAccountCard(acc))
}

function _buildAccountCard(acc) {
    const card = document.getElementById('tpl-account-card').content.firstElementChild.cloneNode(true)
    card.addEventListener('click', () => navigate(`/pages/views/AccountView.html?id=${acc.id}`))

    if (acc.iconKey) {
        const iconEl = card.querySelector('.ac-icon')
        iconEl.innerHTML = `<i class="ph ${acc.iconKey}"></i>`
        iconEl.hidden = false
    }
    card.querySelector('.item-card-name').textContent = acc.name

    const typeRow = card.querySelector('.ac-type')
    typeRow.innerHTML = Icons.accounts()
    typeRow.appendChild(document.createTextNode(' ' + I18n.t(acc.type === 'CREDIT_CARD' ? 'accountTypeCreditCard' : 'accountTypeChecking')))

    const inst = card.querySelector('.ac-institution')
    inst.innerHTML = Icons.institutions()
    inst.appendChild(document.createTextNode(' ' + (acc.financialInstitution || I18n.t('noInstitutionSelected'))))

    if (acc.contact) {
        const contact = card.querySelector('.ac-contact')
        contact.innerHTML = Icons.phone()
        contact.appendChild(document.createTextNode(acc.contact))
        contact.hidden = false
    }

    let balClass = 'zero'
    if (acc.balance > 0) balClass = 'positive'
    else if (acc.balance < 0) balClass = 'negative'
    const bal = card.querySelector('.item-balance')
    bal.classList.add(balClass)
    bal.textContent = `${acc.balance >= 0 ? '+ ' : '- '} $ ${formatCurrency(Math.abs(acc.balance))}`

    return card
}

if (!globalThis.__appRouter) init()
