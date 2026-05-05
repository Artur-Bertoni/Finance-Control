import { doRequest, navigate } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'

SidebarManager.initialize()

const list = document.getElementById('accounts-list')
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
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h8a2 2 0 012 2v12a1 1 0 110 2h-3a1 1 0 01-1-1v-2a1 1 0 00-1-1H9a1 1 0 00-1 1v2a1 1 0 01-1 1H4a1 1 0 110-2V4z" clip-rule="evenodd"/></svg>
            <p>Nenhuma conta cadastrada</p>
        </div>`
} else {
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
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h8a2 2 0 012 2v12a1 1 0 110 2h-3a1 1 0 01-1-1v-2a1 1 0 00-1-1H9a1 1 0 00-1 1v2a1 1 0 01-1 1H4a1 1 0 110-2V4zm3 1h2v2H7V5zm2 4H7v2h2V9zm2-4h2v2h-2V5zm2 4h-2v2h2V9z" clip-rule="evenodd"/></svg>
                    ${acc.financialInstitution || 'Sem instituição'}
                </div>
                ${acc.contact ? `<div class="item-card-row">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"/></svg>
                    ${acc.contact}
                </div>` : ''}
            </div>
            <div class="item-balance ${balClass}">
                ${acc.balance >= 0 ? '+ ' : '- '} $ ${Math.abs(acc.balance).toFixed(2)}
            </div>`

        list.appendChild(card)
    }
}
