import { doRequest, formatDate, navigate, setupSearch, showPendingToast } from '../utils/FrontendFunctions.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'
import { I18n } from './i18n.js'

let allGoals     = []
let searchQuery  = ''
let statusFilter = ''
let showArchived = false

function syncClearBtn() {
    const btn = document.getElementById('clear-search-btn')
    const wrapper = btn?.closest('.filter-clear-field') ?? btn
    if (!wrapper) return
    wrapper.style.display = (searchQuery || statusFilter || showArchived) ? 'flex' : 'none'
}

export function init() {
    document.body.classList.add('page-dashboard')
    SidebarManager.initialize()
    showPendingToast()
    loadData()
    setupSearch(q => { searchQuery = q; renderList() }, () => { searchQuery = ''; renderList() }, syncClearBtn)

    const sel     = document.getElementById('status-filter')
    const archChk = document.getElementById('show-archived-check')
    sel?.addEventListener('change', () => { statusFilter = sel.value; syncClearBtn(); renderList() })
    archChk?.addEventListener('change', () => { showArchived = archChk.checked; syncClearBtn(); renderList() })

    document.getElementById('clear-search-btn')?.addEventListener('click', () => {
        const input = document.getElementById('search-input')
        if (input) input.value = ''
        if (sel) sel.value = ''
        if (archChk) archChk.checked = false
        searchQuery  = ''
        statusFilter = ''
        showArchived = false
        syncClearBtn()
        renderList()
    })

    I18n.onChange(() => {
        populateStatusOptions()
        renderList()
    })
}

function loadData() {
    try {
        allGoals = doRequest('/api/goals', 'GET') ?? []
    } catch {
        allGoals = []
    }
    populateStatusOptions()
    renderList()
}

function populateStatusOptions() {
    const sel = document.getElementById('status-filter')
    if (!sel) return
    const current = sel.value
    sel.innerHTML = `
        <option value="">${I18n.t('allGoals')}</option>
        <option value="active">${I18n.t('activeGoals')}</option>
        <option value="completed">${I18n.t('completedGoals')}</option>
        <option value="expired">${I18n.t('expiredGoals')}</option>
    `
    sel.value = current
}

function renderList() {
    const list = document.getElementById('goals-list')
    if (!list) return
    list.innerHTML = ''

    const q = searchQuery.trim().toLowerCase()
    let goals = allGoals
    if (!showArchived) goals = goals.filter(g => g.status !== 'archived')
    if (q)             goals = goals.filter(g => g.name.toLowerCase().includes(q))
    if (statusFilter)  goals = goals.filter(g => g.status === statusFilter)

    if (goals.length === 0) {
        const empty = document.createElement('div')
        empty.className = 'empty-state'
        empty.style.gridColumn = '1 / -1'
        if (allGoals.length === 0) {
            empty.innerHTML = `${Icons.goals()}<p>${I18n.t('noGoalsEmpty')}</p>`
            const btn = document.createElement('button')
            btn.className = 'btn btn-primary btn-sm'
            btn.textContent = I18n.t('newGoal')
            btn.addEventListener('click', () => navigate('/pages/Goal.html'))
            empty.appendChild(btn)
        } else {
            empty.innerHTML = `${Icons.goals()}<p>${I18n.t('noGoalsRegistered')}</p>`
        }
        list.appendChild(empty)
        return
    }

    for (const g of goals) {
        list.appendChild(buildGoalCard(g))
    }

    const highlightId = new URLSearchParams(location.search).get('highlight')
    if (highlightId) {
        const card = list.querySelector(`[data-goal-id="${highlightId}"]`)
        if (card) {
            card.scrollIntoView({ behavior: 'smooth', block: 'center' })
            card.classList.add('card-highlighted')
            card.addEventListener('animationend', () => card.classList.remove('card-highlighted'), { once: true })
        }
    }
}

function buildGoalCard(g) {
    const card = document.createElement('div')
    card.className = 'item-card goal-card'
    card.dataset.goalId = g.id
    card.addEventListener('click', () => navigate(`/pages/GoalView.html?id=${g.id}`))

    const pct        = Math.min(g.progressPercent ?? 0, 100)
    const exceeded   = g.type === 'expense_limit' && (g.progressPercent ?? 0) > 100
    const barColor   = goalBarColor(g)
    const statusKey  = statusI18nKey(g.status)
    const typeLabel  = typeI18nLabel(g.type)

    const catTags = (g.categories ?? []).map(c =>
        `<span class="goal-tag">${c.name}</span>`).join('')
    const locTags = (g.locales ?? []).map(l =>
        `<span class="goal-tag">${l.name}</span>`).join('')
    const tags = catTags || locTags
        ? `<div class="goal-tags">${catTags}${locTags}</div>`
        : ''

    const formattedCurrent = formatAmount(g.currentAmount ?? 0)
    const formattedTarget  = formatAmount(g.targetAmount  ?? 0)

    card.innerHTML = `
        <div class="item-card-header">
            <span class="item-card-name">${escapeHtml(g.name)}</span>
            <span class="goal-status-badge goal-status-${g.status}">${I18n.t(statusKey)}</span>
        </div>
        <div class="goal-type-label">${typeLabel}</div>
        ${tags}
        <div class="goal-progress-section">
            <div class="goal-progress-bar-bg">
                <div class="goal-progress-bar-fill" style="width:${pct}%;background:${barColor};"></div>
            </div>
            <div class="goal-progress-amounts">
                <span class="goal-amount-current ${exceeded ? 'goal-exceeded' : ''}">${formattedCurrent}</span>
                <span class="goal-amount-target">${formattedTarget}</span>
            </div>
        </div>
        <div class="item-card-meta">
            <span class="item-card-row goal-period">${formatDate(g.startDate)} → ${formatDate(g.endDate)}</span>
        </div>`

    return card
}

function goalBarColor(g) {
    const pct = g.progressPercent ?? 0
    if (g.type === 'expense_limit') {
        if (pct >= 100) return 'var(--color-danger, #EF4444)'
        if (pct >= 90)  return '#EF4444'
        if (pct >= 75)  return '#F97316'
        if (pct >= 50)  return '#EAB308'
        return 'var(--color-primary, #2E7D32)'
    }
    if (pct >= 100) return 'var(--color-primary, #2E7D32)'
    return '#3B82F6'
}

function statusI18nKey(status) {
    const map = {
        active:    'goalStatusActive',
        completed: 'goalStatusCompleted',
        expired:   'goalStatusExpired',
        archived:  'goalStatusArchived',
    }
    return map[status] ?? 'goalStatusActive'
}

function typeI18nLabel(type) {
    const map = {
        expense_limit: () => I18n.t('goalTypeExpenseLimit'),
        savings:       () => I18n.t('goalTypeSavings'),
        income:        () => I18n.t('goalTypeIncome'),
    }
    return (map[type] ?? (() => type))()
}

function formatAmount(value) {
    return new Intl.NumberFormat(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value)
}


function escapeHtml(str) {
    return String(str ?? '').replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;')
}

if (!globalThis.__appRouter) init()
