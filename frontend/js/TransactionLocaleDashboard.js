import { doRequest, navigate, showPendingToast } from '../utils/FrontendFunctions.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'
import { TransactionLocale } from './class/TransactionLocaleClass.js'
import { I18n } from './i18n.js'

function addressRow(address) {
    return '<div class="item-card-meta"><div class="item-card-row">' + Icons.locations() + address + '</div></div>'
}

export function init() {
    SidebarManager.initialize()
    showPendingToast()

    renderList()

    I18n.onChange(renderList)
}

function renderList() {
    const list = document.getElementById('transaction-locales-list')
    if (!list) return
    list.innerHTML = ''

    let locales = []
    try {
        const data = doRequest('/api/transaction-locales', 'GET')
        for (const el of (data ?? [])) locales.push(TransactionLocale.processTransactionLocale(el))
    } catch (e) {
        console.log('Erro ao carregar locais:', e)
    }

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
        card.addEventListener('click', () => navigate(`/pages/TransactionLocale.html?id=${loc.id}`))
        card.innerHTML = `
            <div class="item-card-header">
                <span class="item-card-name">${loc.name}</span>
                <span class="item-card-badge"></span>
            </div>
            ${loc.address ? addressRow(loc.address) : ''}`
        list.appendChild(card)
    }
}

if (!globalThis.__appRouter) init()
