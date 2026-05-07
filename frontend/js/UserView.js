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

if (!globalThis.__appRouter) init()
