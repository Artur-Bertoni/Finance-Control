import { navigate, showConfirm, showToast } from '../utils/FrontendFunctions.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'
import { I18n } from './i18n.js'

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

    renderNotificationFields(user)
    I18n.onChange(() => renderNotificationFields(user))

    if (user.admin) {
        document.getElementById('admin-section-header').style.display = ''
        document.getElementById('admin-section-grid').style.display = ''
        document.getElementById('send-test-email-btn').addEventListener('click', () => {
            const overlay = document.createElement('div')
            overlay.className = 'loading-overlay'
            overlay.innerHTML = '<div class="loading-spinner"></div>'
            document.body.appendChild(overlay)

            $.ajax({
                url:  '/api/admin/email/send-test',
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
        navigate('/pages/User.html')
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
    document.getElementById('detail-language').textContent =
        I18n.t(LANG_KEYS[user.language] ?? 'notInformed')
}

if (!globalThis.__appRouter) init()
