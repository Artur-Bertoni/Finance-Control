import { navigate, showConfirm, showToast } from '../../utils/FrontendFunctions.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { ChangeHistoryManager } from '../components/ChangeHistoryManager.js'
import { Icons } from '../icons/IconLibrary.js'
import { I18n } from '../i18n.js'


export function init() {
    let user = null

    $.ajax({
        url:   '/api/auth/me',
        type:  'GET',
        async: false,
        success: function (u) { user = u },
        error:   function ()  { navigate('/pages/Login.html') }
    })

    if (!user?.id) return

    SidebarManager.initialize()

    document.getElementById('detail-username').textContent = user.username
    document.getElementById('detail-email').textContent    = user.email
    document.querySelector('label[for="detail-google-status"]').insertAdjacentHTML('afterbegin', Icons.google(16))

    renderNotificationFields(user)
    renderGoogleStatus(user)
    I18n.onChange(() => { renderNotificationFields(user); renderGoogleStatus(user) })

    if (user.admin) {
        document.getElementById('admin-section-header').style.display = ''
        document.getElementById('admin-section-grid').style.display = ''
        document.getElementById('send-test-email-btn').addEventListener('click', () => {
            const type    = document.getElementById('test-email-type').value
            const overlay = document.createElement('div')
            overlay.className = 'loading-overlay'
            overlay.innerHTML = '<div class="loading-spinner"></div>'
            document.body.appendChild(overlay)

            $.ajax({
                url:  `/api/admin/email/send-test?type=${encodeURIComponent(type)}`,
                type: 'POST',
                success:  () => showToast(I18n.t('testEmailSent'), 'success'),
                error:    xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorSendingTestEmail'), 'error'),
                complete: () => overlay.remove()
            })
        })
    }

    const actionsEl = document.getElementById('header-actions')

    const logoutBtn = document.createElement('button')
    logoutBtn.className = 'btn btn-ghost btn-sm'
    logoutBtn.type = 'button'
    logoutBtn.innerHTML = `${Icons.logout()}<span style="margin-left:5px" data-i18n="logout">${I18n.t('logout')}</span>`
    logoutBtn.addEventListener('click', () => {
        $.ajax({
            url: '/api/auth/logout', type: 'POST', async: false,
            complete: () => { globalThis.location.href = '/pages/Login.html' }
        })
    })
    actionsEl.insertBefore(logoutBtn, actionsEl.firstChild)

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate('/pages/crud/User.html')
    )

    document.getElementById('delete-btn').addEventListener('click', () => {
        showConfirm(I18n.t('deleteAccountConfirm'), () => {
            $.ajax({
                url:   `/api/users/${user.id}`,
                type:  'DELETE',
                async: false,
                success: () => { globalThis.location.href = '/pages/Login.html' },
                error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingUser'), 'error')
            })
        }, I18n.t('deleteAccountTitle'))
    })

    let historyLoaded = false
    document.querySelectorAll('.view-tab').forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab
            document.querySelectorAll('.view-tab').forEach(b => b.classList.remove('view-tab--active'))
            btn.classList.add('view-tab--active')
            document.getElementById('tab-details').style.display = tab === 'details' ? '' : 'none'
            document.getElementById('tab-history').style.display  = tab === 'history'  ? '' : 'none'
            if (tab === 'history' && !historyLoaded) {
                historyLoaded = true
                ChangeHistoryManager.loadAndRender('user', user.id, user.createdAt, 'history-container')
            }
        })
    })
}

const DAY_KEYS  = ['', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']
const LANG_KEYS = { pt: 'languagePt', en: 'languageEn', es: 'languageEs' }

function renderNotificationFields(user) {
    const statusEl = document.getElementById('detail-notification-status')
    if (!statusEl) return
    const enabled = user.emailNotificationEnabled
    statusEl.textContent = I18n.t(enabled ? 'enabled' : 'disabled')
    statusEl.className   = `tx-badge ${enabled ? 'enabled' : 'disabled'}`
    document.getElementById('detail-notification-day').textContent =
        I18n.t(DAY_KEYS[user.emailNotificationDay] ?? 'notInformed')
    const goalEl = document.getElementById('detail-goal-notification-status')
    if (goalEl) {
        const goalEnabled = user.goalEmailNotificationEnabled !== false
        goalEl.textContent = I18n.t(goalEnabled ? 'enabled' : 'disabled')
        goalEl.className   = `tx-badge ${goalEnabled ? 'enabled' : 'disabled'}`
    }
    document.getElementById('detail-language').textContent =
        I18n.t(LANG_KEYS[user.language] ?? 'notInformed')
}

function renderGoogleStatus(user) {
    const el = document.getElementById('detail-google-status')
    if (!el) return
    const linked = !!user.googleLinked
    el.textContent = I18n.t(linked ? 'linked' : 'notLinked')
    el.className   = `tx-badge ${linked ? 'enabled' : 'disabled'}`
}

if (!globalThis.__appRouter) init()
