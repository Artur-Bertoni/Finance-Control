import { doRequest, navigate } from '../utils/FrontendFunctions.js'
import { SidebarManager } from './components/SidebarManager.js'

SidebarManager.initialize()

const PIN_SVG = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/></svg>'

function addressRow(address) {
    return '<div class="item-card-meta"><div class="item-card-row">' + PIN_SVG + address + '</div></div>'
}
import { TransactionLocale } from './class/TransactionLocaleClass.js'

const list = document.getElementById('transaction-locales-list')
list.innerHTML = ''

let locales = []
try {
    const data = doRequest('/api/transaction-locales', 'GET')
    for (const el of (data ?? [])) locales.push(TransactionLocale.processTransactionLocale(el))
} catch (e) {
    console.log('Erro ao carregar locais:', e)
}

if (locales.length === 0) {
    list.innerHTML = `<div class="empty-state" style="grid-column:1/-1">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path fill-rule="evenodd" d="M11.54 22.351l.07.04.028.016a.76.76 0 00.723 0l.028-.015.071-.041a16.975 16.975 0 001.144-.742 19.58 19.58 0 002.683-2.282c1.944-2.083 3.218-4.402 3.218-6.853C19.5 6.697 16.14 3 12 3S4.5 6.697 4.5 12.234c0 2.451 1.274 4.77 3.218 6.853a19.58 19.58 0 002.683 2.282 16.975 16.975 0 001.144.742zM12 13.5a1.5 1.5 0 100-3 1.5 1.5 0 000 3z" clip-rule="evenodd"/></svg>
        <p>Nenhum local cadastrado</p></div>`
} else {
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
