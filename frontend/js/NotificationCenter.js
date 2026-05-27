import { doRequest, navigate } from '../utils/FrontendFunctions.js'
import { I18n } from './i18n.js'
import { SidebarManager } from './components/SidebarManager.js'
import { MascotManager } from './components/MascotManager.js'
import { FinnySvg } from './utils/FinnySvg.js'

const TYPE_META = {
    GOAL_MILESTONE_50:     { icon: '📊', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 50 } },
    GOAL_MILESTONE_75:     { icon: '📈', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 75 } },
    GOAL_MILESTONE_90:     { icon: '🔥', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 90 } },
    GOAL_COMPLETED:        { icon: '🎯', i18nKey: 'notifGoalCompleted'   },
    GOAL_EXCEEDED:         { icon: '⚠️', i18nKey: 'notifGoalExceeded'   },
    GOAL_DEADLINE_WARNING: { icon: '⏰', i18nKey: 'notifGoalDeadline'   },
}

const TYPE_COLOR = { success: '#22c55e', error: '#ef4444', warning: '#f59e0b', info: '#3b82f6' }

let _activeTab      = 'tips'
let _countdownTimer = null

export function init() {
    FinnySvg.autoInit()
    _stopCountdown()
    _setupTabs()
    _switchTab('tips')
    _syncBadge()
}

function _setupTabs() {
    document.getElementById('nc-tab-tips')?.addEventListener('click',     () => _switchTab('tips'))
    document.getElementById('nc-tab-messages')?.addEventListener('click', () => _switchTab('messages'))
}

function _switchTab(tab) {
    _activeTab = tab

    const tipsEl = document.getElementById('nc-content-tips')
    const msgEl  = document.getElementById('nc-content-messages')
    const tipBtn = document.getElementById('nc-tab-tips')
    const msgBtn = document.getElementById('nc-tab-messages')

    if (tipsEl) tipsEl.hidden = tab !== 'tips'
    if (msgEl)  msgEl.hidden  = tab !== 'messages'
    tipBtn?.classList.toggle('finny-page-tab--active', tab === 'tips')
    msgBtn?.classList.toggle('finny-page-tab--active', tab === 'messages')

    if (tab === 'tips')     { _loadTips() }
    if (tab === 'messages') { _stopCountdown(); _loadMessages() }
}

function _loadTips() {
    const tipEl = document.getElementById('nc-tip-text')
    if (tipEl) tipEl.textContent = MascotManager._currentTip || ''
    _startCountdown()
}

function _startCountdown() {
    _stopCountdown()
    const tick = () => {
        const el = document.getElementById('nc-tip-countdown')
        if (!el) { _stopCountdown(); return }
        const ms = Math.max(0, MascotManager._nextTipTime - Date.now())
        el.textContent = `${I18n.t('tipCountdownLabel')} ${MascotManager._formatCountdown(ms)}`
    }
    tick()
    _countdownTimer = setInterval(tick, 1000)
}

function _stopCountdown() {
    if (_countdownTimer) { clearInterval(_countdownTimer); _countdownTimer = null }
}

function _loadMessages() {
    const list = document.getElementById('nc-notifications-list')
    if (!list) return

    list.innerHTML = `<div class="mascot-notif-empty mascot-notif-loading">…</div>`

    const allNotifs = doRequest('/api/notifications', 'GET') ?? []

    list.innerHTML = ''

    if (allNotifs.length === 0) {
        list.innerHTML = `<div class="mascot-notif-empty">${I18n.t('mascotNoMessages')}</div>`
        document.getElementById('nc-mark-all-read').style.display = 'none'
        return
    }

    document.getElementById('nc-mark-all-read').style.display = ''

    for (const item of allNotifs) {
        list.appendChild(item.type === 'USER_ACTION' ? _buildLocalCard(item) : _buildBackendCard(item))
    }

    document.getElementById('nc-mark-all-read')?.addEventListener('click', () => {
        doRequest('/api/notifications/read-all', 'PUT')
        SidebarManager.refreshNotificationBadge()
        MascotManager.refreshBadge()
        _loadMessages()
        _syncBadge()
    })
}

function _buildBackendCard(n) {
    const meta    = TYPE_META[n.type] ?? { icon: '🔔', i18nKey: 'notifications' }
    const label   = I18n.t(meta.i18nKey, meta.i18nParams)
    const dateStr = new Date(n.createdAt).toLocaleString(I18n.getLanguage(), { dateStyle: 'short', timeStyle: 'short' })

    const card = document.createElement('div')
    card.className = `mascot-notif-card finny-page-notif-card${n.read ? ' mascot-notif-card--read' : ''}`
    card.innerHTML = `
        <span class="mascot-notif-icon">${meta.icon}</span>
        <div class="mascot-notif-body">
            <p class="mascot-notif-title">${label}: <strong>${escapeHtml(n.goalName ?? '')}</strong></p>
            <p class="mascot-notif-date">${dateStr}</p>
        </div>
        <div class="mascot-notif-actions">
            ${n.link ? `<button class="btn btn-ghost btn-sm notif-view-btn">${I18n.t('view')}</button>` : ''}
            ${n.read ? '' : `<button class="btn btn-ghost btn-sm notif-read-btn">${I18n.t('markAsRead')}</button>`}
        </div>
    `

    card.querySelector('.notif-view-btn')?.addEventListener('click', () => {
        _markRead(n.id, card)
        navigate(n.link)
    })
    card.querySelector('.notif-read-btn')?.addEventListener('click', () => {
        _markRead(n.id, card)
    })

    return card
}

function _buildLocalCard(n) {
    const color   = TYPE_COLOR[n.severity] ?? TYPE_COLOR.info
    const dateStr = new Date(n.createdAt).toLocaleString(I18n.getLanguage(), { dateStyle: 'short', timeStyle: 'short' })

    const card = document.createElement('div')
    card.className = 'mascot-notif-card finny-page-notif-card mascot-notif-card--read mascot-notif-card--local'
    card.style.setProperty('--local-card-color', color)
    card.innerHTML = `
        <span class="mascot-notif-local-bar"></span>
        <div class="mascot-notif-body">
            <p class="mascot-notif-title">${escapeHtml(n.message)}</p>
            <p class="mascot-notif-date">${dateStr}</p>
        </div>
        ${n.link ? `<div class="mascot-notif-actions"><button class="btn btn-ghost btn-sm local-view-btn">${I18n.t('view')}</button></div>` : ''}
    `
    card.querySelector('.local-view-btn')?.addEventListener('click', () => navigate(n.link))
    return card
}

function _markRead(id, card) {
    doRequest(`/api/notifications/${id}/read`, 'PUT')
    card.classList.add('mascot-notif-card--read')
    card.querySelector('.notif-read-btn')?.remove()
    SidebarManager.refreshNotificationBadge()
    MascotManager.refreshBadge()
    _syncBadge()
}

function _syncBadge() {
    try {
        const raw   = doRequest('/api/notifications/unread-count', 'GET')
        const count = Math.max(0, Number(raw) || 0)
        const badge = document.getElementById('nc-tab-badge')
        if (badge) { badge.textContent = count > 99 ? '99+' : String(count); badge.hidden = false }
    } catch { /* silencioso */ }
}

function escapeHtml(str) {
    return String(str ?? '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;')
}
