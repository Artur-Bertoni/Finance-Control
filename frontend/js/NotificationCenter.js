import { doRequest, navigate, showToast } from '../utils/FrontendFunctions.js'
import { I18n } from './i18n.js'
import { SidebarManager } from './components/SidebarManager.js'

const TYPE_META = {
    GOAL_MILESTONE_50:    { icon: '📊', i18nKey: 'notifGoalMilestone50', toastType: 'info'    },
    GOAL_MILESTONE_75:    { icon: '📈', i18nKey: 'notifGoalMilestone75', toastType: 'info'    },
    GOAL_MILESTONE_90:    { icon: '🔥', i18nKey: 'notifGoalMilestone90', toastType: 'warning' },
    GOAL_COMPLETED:       { icon: '🎯', i18nKey: 'notifGoalCompleted',   toastType: 'success' },
    GOAL_EXCEEDED:        { icon: '⚠️', i18nKey: 'notifGoalExceeded',   toastType: 'warning' },
    GOAL_DEADLINE_WARNING:{ icon: '⏰', i18nKey: 'notifGoalDeadline',   toastType: 'warning' },
}

export function init() {
    SidebarManager.initialize()
    loadNotifications()

    document.getElementById('mark-all-read-btn').addEventListener('click', () => {
        $.ajax({
            url: '/api/notifications/read-all', type: 'PUT', async: false,
            success: () => { loadNotifications(); SidebarManager.refreshNotificationBadge() },
            error:   ()  => showToast(I18n.t('errorGeneric'), 'error')
        })
    })
}

function loadNotifications() {
    const notifications = doRequest('/api/notifications', 'GET') ?? []
    const list          = document.getElementById('notifications-list')
    const empty         = document.getElementById('notifications-empty')

    list.querySelectorAll('.notification-card').forEach(el => el.remove())

    if (notifications.length === 0) {
        empty.style.display = ''
        document.getElementById('mark-all-read-btn').style.display = 'none'
        return
    }

    empty.style.display = 'none'
    document.getElementById('mark-all-read-btn').style.display = ''

    for (const n of notifications) {
        list.appendChild(buildCard(n))
    }
}

function buildCard(n) {
    const meta    = TYPE_META[n.type] ?? { icon: '🔔', i18nKey: 'notifications', toastType: 'info' }
    const label   = I18n.t(meta.i18nKey)
    const dateStr = new Date(n.createdAt).toLocaleString(I18n.getLanguage(), { dateStyle: 'short', timeStyle: 'short' })

    const card = document.createElement('div')
    card.className = `notification-card${n.read ? ' notification-card--read' : ''}`
    card.dataset.id = n.id
    card.innerHTML = `
        <div class="notification-card__icon">${meta.icon}</div>
        <div class="notification-card__body">
            <p class="notification-card__title">${label}: <strong>${escapeHtml(n.goalName ?? '')}</strong></p>
            <p class="notification-card__date">${dateStr}</p>
        </div>
        <div class="notification-card__actions">
            ${n.link ? `<button class="btn btn-ghost btn-sm notif-view-btn" data-i18n="view">${I18n.t('view')}</button>` : ''}
            ${!n.read ? `<button class="btn btn-ghost btn-sm notif-read-btn" data-i18n="markAsRead">${I18n.t('markAsRead')}</button>` : ''}
        </div>
    `

    card.querySelector('.notif-view-btn')?.addEventListener('click', () => {
        markRead(n.id, card)
        navigate(n.link)
    })

    card.querySelector('.notif-read-btn')?.addEventListener('click', () => {
        markRead(n.id, card)
    })

    return card
}

function markRead(id, card) {
    $.ajax({
        url: `/api/notifications/${id}/read`, type: 'PUT', async: false,
        success: () => {
            card.classList.add('notification-card--read')
            card.querySelector('.notif-read-btn')?.remove()
            SidebarManager.refreshNotificationBadge()
        }
    })
}

function escapeHtml(str) {
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
}

export { TYPE_META }

if (!globalThis.__appRouter) init()
