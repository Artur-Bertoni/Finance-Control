import { doRequest, navigate, setupSearch, showPendingToast } from '../utils/FrontendFunctions.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'
import { TransactionLocale } from './class/TransactionLocaleClass.js'
import { I18n } from './i18n.js'

let allLocales = []
let searchQuery = ''

export function init() {
    document.body.classList.add('page-dashboard')
    SidebarManager.initialize()
    showPendingToast()
    loadData()
    setupSearch(q => { searchQuery = q; renderList() }, () => { searchQuery = ''; renderList() })
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

    const q = searchQuery.trim().toLowerCase()
    const locales = q ? allLocales.filter(l => l.name.toLowerCase().includes(q)) : allLocales

    if (locales.length === 0) {
        list.innerHTML = `
            <div class="empty-state" style="grid-column:1/-1">
                ${Icons.emptyLocation()}
                <p>${I18n.t('noLocalesRegistered')}</p>
            </div>`
        return
    }

    for (const loc of locales) {
        const card = document.createElement('div')
        card.className = 'item-card'
        card.addEventListener('click', () => navigate(`/pages/TransactionLocaleView.html?id=${loc.id}`))
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
