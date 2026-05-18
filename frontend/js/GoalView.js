import { doRequest, navigate, navigateWithToast, setBreadcrumb, showConfirm, showToast } from '../utils/FrontendFunctions.js'
import { SidebarManager } from './components/SidebarManager.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()

    const goalId = new URLSearchParams(globalThis.location.search).get('id')
    if (!goalId) { navigate('/pages/GoalDashboard.html'); return }

    const goal = doRequest(`/api/goals/${goalId}`, 'GET')
    if (!goal?.id) { navigate('/pages/GoalDashboard.html'); return }

    setBreadcrumb([
        { i18nKey: 'goals', url: '/pages/GoalDashboard.html' },
        { label: goal.name }
    ])

    const titleEl = document.getElementById('page-title-text')
    if (titleEl) titleEl.textContent = goal.name

    document.getElementById('detail-name').textContent       = goal.name
    document.getElementById('detail-target').textContent     = formatAmount(goal.targetAmount ?? 0)
    document.getElementById('detail-start-date').textContent = formatDate(goal.startDate)
    document.getElementById('detail-end-date').textContent   = formatDate(goal.endDate)

    const descEl = document.getElementById('detail-description')
    if (goal.description) {
        descEl.textContent = goal.description
    } else {
        descEl.innerHTML = `<span class="detail-empty"></span>`
    }

    const pct = goal.progressPercent ?? 0
    document.getElementById('detail-progress-bar').style.cssText =
        `width:${Math.min(pct, 100)}%;background:${goalBarColor(goal)}`

    const currentEl = document.getElementById('detail-current')
    currentEl.textContent = formatAmount(goal.currentAmount ?? 0)
    if (pct > 100 && goal.type === 'expense_limit') currentEl.classList.add('goal-exceeded')

    document.getElementById('detail-goal-target').textContent = formatAmount(goal.targetAmount ?? 0)
    document.getElementById('detail-pct').textContent         = `${pct.toFixed(1)}%`

    const cats = goal.categories ?? []
    const locs  = goal.locales    ?? []
    if (cats.length || locs.length) {
        document.getElementById('detail-filters-section').style.display = ''
        if (cats.length) renderTags('detail-categories-field', 'detail-categories', cats)
        if (locs.length) renderTags('detail-locales-field',    'detail-locales',    locs)
    }

    renderDynamic(goal)
    I18n.onChange(() => renderDynamic(goal))

    const archiveBtn = document.getElementById('archive-btn')
    if (goal.status === 'active') {
        archiveBtn.style.display = ''
        archiveBtn.addEventListener('click', () =>
            showConfirm(I18n.t('goalArchiveConfirm'), () => {
                $.ajax({
                    url: `/api/goals/${goalId}/archive`, type: 'PUT', async: false,
                    success: () => navigateWithToast('/pages/GoalDashboard.html', I18n.t('goalArchivedSuccess'), 'success'),
                    error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorArchivingGoal'), 'error')
                })
            }, I18n.t('confirmAction'))
        )
    }

    document.getElementById('delete-btn').addEventListener('click', () =>
        showConfirm(I18n.t('goalDeleteConfirm'), () => {
            $.ajax({
                url: `/api/goals/${goalId}`, type: 'DELETE', async: false,
                success: () => navigateWithToast('/pages/GoalDashboard.html', I18n.t('goalDeletedSuccess'), 'success'),
                error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingGoal'), 'error')
            })
        }, I18n.t('confirmAction'))
    )

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate(`/pages/Goal.html?id=${goalId}`)
    )
}

function renderDynamic(goal) {
    document.getElementById('detail-type').innerHTML =
        `<span class="goal-status-badge goal-type-badge">${typeI18nLabel(goal.type)}</span>`
    document.getElementById('detail-status').innerHTML =
        `<span class="goal-status-badge goal-status-${goal.status}">${I18n.t(statusI18nKey(goal.status))}</span>`
    renderNotifications(goal)
}

function renderTags(fieldId, containerId, items) {
    document.getElementById(fieldId).style.display = ''
    const container = document.getElementById(containerId)
    items.forEach(item => {
        const tag = document.createElement('span')
        tag.className   = 'goal-tag'
        tag.textContent = item.name
        container.appendChild(tag)
    })
}

function renderNotifications(goal) {
    const container = document.getElementById('detail-notifications')
    if (!container) return
    const items = [
        { key: 'goalNotifyAt50',       enabled: goal.notifyAt50 },
        { key: 'goalNotifyAt75',       enabled: goal.notifyAt75 },
        { key: 'goalNotifyAt90',       enabled: goal.notifyAt90 },
        { key: 'goalNotifyOnComplete', enabled: goal.notifyOnComplete },
        { key: 'goalNotifyOnDeadline', enabled: goal.notifyOnDeadline },
        ...(goal.type === 'expense_limit'
            ? [{ key: 'goalNotifyOnExceed', enabled: goal.notifyOnExceed }]
            : [])
    ]
    container.innerHTML = ''
    items.forEach(({ key, enabled }) => {
        const badge = document.createElement('span')
        badge.className   = `tx-badge ${enabled ? 'enabled' : 'disabled'}`
        badge.textContent = I18n.t(key)
        container.appendChild(badge)
    })
}

function goalBarColor(g) {
    const pct = g.progressPercent ?? 0
    if (g.type === 'expense_limit') {
        if (pct >= 100) return 'var(--danger, #EF4444)'
        if (pct >= 90)  return '#EF4444'
        if (pct >= 75)  return '#F97316'
        if (pct >= 50)  return '#EAB308'
        return 'var(--primary, #2E7D32)'
    }
    return pct >= 100 ? 'var(--primary, #2E7D32)' : '#3B82F6'
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

function formatDate(dateStr) {
    if (!dateStr) return ''
    const [y, m, d] = dateStr.split('-')
    return `${d}/${m}/${y}`
}

if (!globalThis.__appRouter) init()
