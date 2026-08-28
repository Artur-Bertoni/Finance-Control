import { navigate, showToast } from '../../utils/FrontendFunctions.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { ChangeHistoryManager } from '../components/ChangeHistoryManager.js'
import { AchievementsPanel } from '../components/AchievementsPanel.js'
import { Icons } from '../icons/IconLibrary.js'
import { I18n } from '../i18n.js'
import { UserSettings } from '../utils/UserSettings.js'

import { FEATURE_ROWS } from '../utils/FeatureCatalog.js'


const TAB_IDS = new Set(['details', 'achievements', 'history', 'settings'])

let historyDirty = false

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

    if (!user.emailVerified) {
        const resendBtn = document.createElement('button')
        resendBtn.className = 'btn btn-secondary btn-sm'
        resendBtn.type = 'button'
        resendBtn.style.marginTop = '8px'
        resendBtn.textContent = I18n.t('resendVerificationEmail')
        resendBtn.addEventListener('click', () => {
            if (resendBtn.disabled) return
            const originalLabel = resendBtn.textContent
            resendBtn.disabled = true
            resendBtn.classList.add('is-loading')
            resendBtn.textContent = I18n.t('resendingVerificationEmail')
            $.ajax({
                url:      `/api/auth/resend-verification?email=${encodeURIComponent(user.email)}`,
                type:     'POST',
                success:  () => showToast(I18n.t('verificationEmailSent'), 'success'),
                error:    () => showToast(I18n.t('errorResendVerification'), 'error'),
                complete: () => {
                    resendBtn.disabled = false
                    resendBtn.classList.remove('is-loading')
                    resendBtn.textContent = originalLabel
                }
            })
        })
        document.getElementById('detail-email').insertAdjacentElement('afterend', resendBtn)
    }

    renderNotificationFields(user)
    renderGoogleStatus(user)
    I18n.onChange(() => { renderNotificationFields(user); renderGoogleStatus(user) })

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

    renderSettings()
    I18n.onChange(renderSettings)

    let historyLoaded      = false
    let achievementsLoaded = false
    historyDirty = false

    const params    = new URLSearchParams(location.search)
    const highlight = params.get('highlight')

    const activateTab = tab => {
        document.querySelectorAll('.view-tab').forEach(b =>
            b.classList.toggle('view-tab--active', b.dataset.tab === tab)
        )
        for (const id of TAB_IDS) {
            document.getElementById(`tab-${id}`).style.display = id === tab ? '' : 'none'
        }
        if (tab === 'history' && (!historyLoaded || historyDirty)) {
            historyLoaded = true
            historyDirty  = false
            ChangeHistoryManager.loadAndRender('user', user.id, user.createdAt, 'history-container')
        }
        if (tab === 'achievements' && !achievementsLoaded) {
            achievementsLoaded = true
            AchievementsPanel.render('achievements-grid', highlight)
        }
    }

    document.querySelectorAll('.view-tab').forEach(btn =>
        btn.addEventListener('click', () => activateTab(btn.dataset.tab))
    )

    I18n.onChange(() => {
        if (achievementsLoaded) AchievementsPanel.render('achievements-grid')
    })

    const initialTab = params.get('tab')
    if (initialTab && TAB_IDS.has(initialTab)) activateTab(initialTab)
}

function renderSettings() {
    const list = document.getElementById('settings-list')
    const tpl  = document.getElementById('tpl-settings-row')
    if (!list || !tpl) return

    const settings = UserSettings.all()
    list.innerHTML = ''

    for (const row of FEATURE_ROWS) {
        const el    = tpl.content.firstElementChild.cloneNode(true)
        const title = I18n.t(row.titleKey)
        const input = el.querySelector('input')
        const info  = el.querySelector('.settings-row-info')

        el.querySelector('.settings-row-name').textContent = title
        el.querySelector('.settings-row-desc').textContent = I18n.t(row.descKey)
        info.dataset.tooltip = I18n.t(row.offKey)
        info.setAttribute('aria-label', title)
        input.checked = settings[row.key] !== false
        input.setAttribute('aria-label', title)
        input.addEventListener('change', () => saveSetting(row.key, input))

        list.appendChild(el)
    }
}

function saveSetting(key, input) {
    const desired = input.checked
    input.disabled = true

    $.ajax({
        url:         '/api/user-settings',
        type:        'PUT',
        contentType: 'application/json',
        data:        JSON.stringify({ [key]: desired }),
        success: settings => {
            UserSettings.store(settings)
            historyDirty = true
            SidebarManager.applyFeatureVisibility()
            SidebarManager.applyFinnyLink()
            showToast(I18n.t('settingsSaved'), 'success', null, { saveToHistory: false })
        },
        error: () => {
            input.checked = !desired
            showToast(I18n.t('errorGeneric'), 'error')
        },
        complete: () => { input.disabled = false }
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
        I18n.t(DAY_KEYS[user.emailNotificationDay] ?? 'commonNotInformed')
    const goalEl = document.getElementById('detail-goal-notification-status')
    if (goalEl) {
        const goalEnabled = user.goalEmailNotificationEnabled !== false
        goalEl.textContent = I18n.t(goalEnabled ? 'enabled' : 'disabled')
        goalEl.className   = `tx-badge ${goalEnabled ? 'enabled' : 'disabled'}`
    }
    document.getElementById('detail-language').textContent =
        I18n.t(LANG_KEYS[user.language] ?? 'commonNotInformed')
}

function renderGoogleStatus(user) {
    const el = document.getElementById('detail-google-status')
    if (!el) return
    const linked = !!user.googleLinked
    el.textContent = I18n.t(linked ? 'linked' : 'notLinked')
    el.className   = `tx-badge ${linked ? 'enabled' : 'disabled'}`
}

if (!globalThis.__appRouter) init()
