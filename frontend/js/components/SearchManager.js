import { doRequest, navigate } from '../../utils/FrontendFunctions.js'
import { I18n } from '../i18n.js'

const ENTITY_TYPES = [
    { key: 'accounts',     shortcut: 'A', api: '/api/accounts',              viewPath: '/pages/views/AccountView.html',              dashboardPath: '/pages/lists/AccountList.html',              i18nKey: 'accounts' },
    { key: 'goals',        shortcut: 'G', api: '/api/goals',                 viewPath: '/pages/views/GoalView.html',                 dashboardPath: '/pages/lists/GoalList.html',                 i18nKey: 'goals' },
    { key: 'categories',   shortcut: 'C', api: '/api/categories',            viewPath: '/pages/views/CategoryView.html',             dashboardPath: '/pages/lists/CategoryList.html',             i18nKey: 'categories' },
    { key: 'institutions', shortcut: 'I', api: '/api/financial-institutions', viewPath: '/pages/views/FinancialInstitutionView.html', dashboardPath: '/pages/lists/FinancialInstitutionList.html', i18nKey: 'financialInstitutions' },
    { key: 'locales',      shortcut: 'L', api: '/api/transaction-locales',   viewPath: '/pages/views/TransactionLocaleView.html',    dashboardPath: '/pages/lists/TransactionLocaleList.html',    i18nKey: 'locations' },
]

let _open    = false
let _data    = {}
let _results = {}
let _query   = ''

export class SearchManager {
    static initialize() {
        const toggleBtn  = document.getElementById('search-toggle-btn')
        const panel      = document.getElementById('search-panel')
        const input      = document.getElementById('search-input')
        if (!toggleBtn || !panel || !input) return

        toggleBtn.addEventListener('click', () => {
            _open = !_open
            panel.classList.toggle('open', _open)
            toggleBtn.setAttribute('aria-expanded', String(_open))
            if (_open) {
                _loadData()
                input.focus()
            } else {
                _query   = ''
                input.value = ''
                _results = {}
                _renderResults()
            }
        })

        input.addEventListener('input', e => {
            _query = e.target.value.trim()
            _search()
        })

        input.addEventListener('keydown', e => {
            if (e.key !== 'Escape') return
            e.preventDefault()
            _open = false
            _query = ''
            input.value = ''
            _results = {}
            panel.classList.remove('open')
            toggleBtn.setAttribute('aria-expanded', 'false')
            _renderResults()
            input.blur()
        })

        document.addEventListener('keydown', e => {
            if (e.ctrlKey && e.shiftKey && e.key.toUpperCase() === 'F') {
                e.preventDefault()
                SearchManager.open()
                return
            }
            if (!e.shiftKey || e.ctrlKey || e.altKey || e.metaKey) return
            const active        = document.activeElement
            const isSearchInput = active?.id === 'search-input'
            if (!isSearchInput && active &&
                (active.tagName === 'INPUT' || active.tagName === 'TEXTAREA' || active.tagName === 'SELECT')) return

            const key    = e.key.toUpperCase()
            const entity = ENTITY_TYPES.find(t => t.shortcut === key)
            if (!entity) return

            if (isSearchInput) {
                const hits = _results[entity.key]
                if (hits?.length) {
                    e.preventDefault()
                    navigate(`${entity.viewPath}?id=${hits[0].id}`)
                }
                return
            }
            e.preventDefault()
            navigate(entity.dashboardPath)
        })

        _renderShortcutsHint()
        I18n.onChange(() => { _renderShortcutsHint(); _renderResults() })
    }

    static open() {
        const toggleBtn = document.getElementById('search-toggle-btn')
        const panel     = document.getElementById('search-panel')
        const input     = document.getElementById('search-input')
        if (!panel || !input) return
        if (!_open) {
            _open = true
            panel.classList.add('open')
            if (toggleBtn) toggleBtn.setAttribute('aria-expanded', 'true')
            _loadData()
        }
        input.focus()
        input.select()
    }

    static invalidateCache() {
        _data = {}
    }

    static reset() {
        const panel = document.getElementById('search-panel')
        const input = document.getElementById('search-input')
        _open    = false
        _query   = ''
        _results = {}
        if (panel) panel.classList.remove('open')
        if (input) input.value = ''
        _renderResults()
    }
}

function _loadData() {
    if (Object.keys(_data).length > 0) return
    for (const type of ENTITY_TYPES) {
        try { _data[type.key] = doRequest(type.api, 'GET') ?? [] }
        catch { _data[type.key] = [] }
    }
}

function _search() {
    _results = {}
    const q = _query.toLowerCase()
    if (!q) { _renderResults(); return }
    for (const type of ENTITY_TYPES) {
        const items = _data[type.key] ?? []
        _results[type.key] = items.filter(item =>
            (item.name ?? item.alias ?? '').toLowerCase().includes(q)
        )
    }
    _renderResults()
}

function _renderResults() {
    const container = document.getElementById('search-results')
    if (!container) return

    if (!_query) { container.innerHTML = ''; return }

    let html = ''
    let any  = false

    for (const type of ENTITY_TYPES) {
        const hits = _results[type.key] ?? []
        if (!hits.length) continue
        any = true
        html += `<div class="search-group">
            <div class="search-group-header">
                <span>${I18n.t(type.i18nKey)}</span>
                <kbd class="search-kbd">Shift+${type.shortcut}</kbd>
            </div>`
        for (const item of hits.slice(0, 3)) {
            html += `<button class="search-result-item" data-href="${type.viewPath}?id=${item.id}">${item.name ?? item.alias ?? ''}</button>`
        }
        if (hits.length > 3) {
            html += `<span class="search-more">+${hits.length - 3} ${I18n.t('commonMoreResults')}</span>`
        }
        html += `</div>`
    }

    if (!any) {
        html = `<div class="search-empty">${I18n.t('noSearchResults', { query: _query })}</div>`
    }

    container.innerHTML = html
    container.querySelectorAll('.search-result-item').forEach(btn => {
        btn.addEventListener('click', () => navigate(btn.dataset.href))
    })
}

function _renderShortcutsHint() {
    const hint = document.getElementById('search-shortcuts-hint')
    if (!hint) return
    hint.innerHTML = ENTITY_TYPES.map(t =>
        `<div class="shortcut-hint-row">
            <kbd class="search-kbd">Shift+${t.shortcut}</kbd>
            <span>${I18n.t(t.i18nKey)}</span>
        </div>`
    ).join('')
}
