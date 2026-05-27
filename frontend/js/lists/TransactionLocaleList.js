import { doRequest, navigate, setupSearch, showPendingToast, initFilterToggle, createAddCard } from '../../utils/FrontendFunctions.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { Icons } from '../icons/IconLibrary.js'
import { TransactionLocale } from '../class/TransactionLocaleClass.js'
import { I18n } from '../i18n.js'
import { createEmptyState } from '../components/EmptyState.js'

let allLocales = []
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
        const data = doRequest('/api/transaction-locales', 'GET')
        allLocales = (data ?? []).map(el => TransactionLocale.processTransactionLocale(el))
    } catch {
        allLocales = []
    }
    renderList()
}

function renderList() {
    const list = document.getElementById('transaction-locales-list')
    if (!list) return
    list.innerHTML = ''

    list.appendChild(createAddCard(I18n.t('newLocale'), '/pages/crud/TransactionLocale.html'))

    const q = searchQuery.trim().toLowerCase()
    const locales = q ? allLocales.filter(l => l.name.toLowerCase().includes(q)) : allLocales

    if (locales.length === 0) {
        list.appendChild(createEmptyState(Icons.locations(), I18n.t(allLocales.length === 0 ? 'noLocalesEmpty' : 'noLocalesRegistered')))
        return
    }

    for (const loc of locales) {
        const card = document.createElement('div')
        card.className = 'item-card'
        card.addEventListener('click', () => navigate(`/pages/views/TransactionLocaleView.html?id=${loc.id}`))
        const iconHtml = loc.iconKey
            ? `<span style="font-size:18px;color:var(--primary);flex-shrink:0"><i class="ph ${loc.iconKey}"></i></span>`
            : ''
        const addressHtml = loc.address
            ? '<div class="item-card-meta"><div class="item-card-row">' + Icons.locations() + loc.address + '</div></div>'
            : ''
        card.innerHTML = `
            <div class="item-card-header">
                <span class="item-card-name-group">${iconHtml}<span class="item-card-name">${loc.name}</span></span>
                <span class="item-card-badge"></span>
            </div>
            ${addressHtml}`
        list.appendChild(card)
    }
}

if (!globalThis.__appRouter) init()
