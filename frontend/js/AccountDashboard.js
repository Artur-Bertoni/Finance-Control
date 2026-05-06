import { doRequest, navigate, showPendingToast } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()
    showPendingToast()

    renderList()

    I18n.onChange(renderList)
}

function renderList() {
    const list = document.getElementById('accounts-list')
    if (!list) return
    list.innerHTML = ''

    let accounts = []
    try {
        const data = doRequest('/api/accounts', 'GET')
        for (const el of (data ?? [])) accounts.push(Account.processAccount(el))
    } catch (e) {
        console.log('Erro ao carregar contas:', e)
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
        card.addEventListener('click', () => navigate(`/pages/Account.html?id=${acc.id}`))

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
                ${acc.balance >= 0 ? '+ ' : '- '} $ ${Math.abs(acc.balance).toFixed(2)}
            </div>`

        list.appendChild(card)
    }
}

if (!globalThis.__appRouter) init()
