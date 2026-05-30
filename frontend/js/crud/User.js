import { clearDirtyGuard, navigate, setBreadcrumb, setupDirtyGuard, showToast } from '../../utils/FrontendFunctions.js'
import { PasswordInput } from '../components/PasswordInput.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { ThemeManager } from '../ThemeManager.js'
import { Icons } from '../icons/IconLibrary.js'

import { setupRequiredFieldValidation, validateRequiredFields } from '../utils/FieldValidation.js'
import { I18n } from '../i18n.js'

let currentUser = null

export function init() {
    currentUser = null

    PasswordInput.setupToggle('password-input', 'password-img')
    PasswordInput.setupToggle('password-confirm-input', 'password-confirm-img')

    setupRequiredFieldValidation([
        'username-input', 'email-input', 'password-input', 'password-confirm-input'
    ])

    loadUserData()

    document.getElementById('cancel-btn').addEventListener('click', () =>
        navigate(currentUser ? '/pages/views/UserView.html' : '/pages/Login.html')
    )

    document.getElementById('save-btn').addEventListener('click', handleSave)

    setupDirtyGuard()
}

function handleSave() {
    const isNewUser = !currentUser

    if (isNewUser) {
        const fieldLabels = {
            'username-input':         I18n.t('username'),
            'email-input':            I18n.t('emailAddress'),
            'password-input':         I18n.t('password'),
            'password-confirm-input': I18n.t('confirmPassword')
        }
        const emptyFields = validateRequiredFields(
            ['username-input', 'email-input', 'password-input', 'password-confirm-input'],
            fieldLabels
        )
        if (emptyFields.length > 0) {
            showToast(I18n.t('commonFillRequired', { fields: emptyFields.join(', ') }), 'warning')
            return
        }

        const notifyEnabled = document.getElementById('guest-notification-enabled-input').checked
        const body = {
            username:                   document.getElementById('username-input').value,
            email:                      document.getElementById('email-input').value,
            password:                   document.getElementById('password-input').value,
            passwordConfirmation:       document.getElementById('password-confirm-input').value,
            emailNotificationEnabled:   notifyEnabled,
            emailNotificationDay:       notifyEnabled
                ? Number.parseInt(document.getElementById('guest-notification-day-input').value, 10)
                : 5
        }

        $.ajax({
            url:         '/api/users',
            type:        'POST',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify(body),
            success: function () {
                $.ajax({
                    url:         '/api/auth/login',
                    type:        'POST',
                    async:       false,
                    contentType: 'application/json',
                    data:        JSON.stringify({ email: body.email, password: body.password }),
                    success: function () {
                        clearDirtyGuard()
                        showToast(I18n.t('accountCreatedLoginSuccess'), 'success')
                        navigate('/pages/HomePage.html')
                    },
                    error: function () {
                        showToast(I18n.t('accountCreatedLoginFail'), 'warning')
                        navigate('/pages/Login.html')
                    }
                })
            },
            error: function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingUser'), 'error') }
        })
    } else {
        const fieldLabels = {
            'username-input': I18n.t('username'),
            'email-input':    I18n.t('emailAddress')
        }
        const emptyFields = validateRequiredFields(['username-input', 'email-input'], fieldLabels)
        if (emptyFields.length > 0) {
            showToast(I18n.t('commonFillRequired', { fields: emptyFields.join(', ') }), 'warning')
            return
        }

        const selectedLanguage = document.getElementById('language-select').value
        $.ajax({
            url:         `/api/users/${currentUser.id}`,
            type:        'PUT',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify({
                username:                      document.getElementById('username-input').value,
                email:                         document.getElementById('email-input').value,
                emailNotificationEnabled:      document.getElementById('notification-enabled-input').checked,
                emailNotificationDay:          Number.parseInt(document.getElementById('notification-day-input').value, 10),
                goalEmailNotificationEnabled:  document.getElementById('goal-notification-enabled-input').checked,
                language:                      selectedLanguage
            }),
            success: function () {
                clearDirtyGuard()
                I18n.setLanguage(selectedLanguage)
                navigate('/pages/views/UserView.html')
            },
            error: function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingUser'), 'error') }
        })
    }
}

function showGuestForm() {
    ThemeManager.initialize()
    document.body.classList.add('user-guest')

    const main   = document.querySelector('.page-content')
    const header = document.createElement('div')
    header.className = 'guest-form-header'
    header.innerHTML = `<img src="../images/logo.png" alt="Finance Control"><h1 data-i18n="createAccount">${I18n.t('createAccount')}</h1><p data-i18n="guestFormHeader">${I18n.t('guestFormHeader')}</p>`
    main.insertBefore(header, main.firstChild)

    const titleEl = document.getElementById('page-title-text')
    if (titleEl) { titleEl.dataset.i18n = 'createAccount'; titleEl.textContent = I18n.t('createAccount') }

    const saveBtn = document.getElementById('save-btn')
    if (saveBtn) { saveBtn.dataset.i18n = 'createAccount'; saveBtn.textContent = I18n.t('createAccount') }

    const cancelBtn = document.getElementById('cancel-btn')
    if (cancelBtn) { cancelBtn.dataset.i18n = 'backToLogin'; cancelBtn.textContent = I18n.t('backToLogin') }

    setupGuestNotificationSection()
}

function setupGuestNotificationSection() {
    document.getElementById('guest-notification-section').style.display = ''

    const checkbox  = document.getElementById('guest-notification-enabled-input')
    const label     = document.getElementById('guest-notification-enabled-label')
    const dayField  = document.getElementById('field-guest-notification-day')

    function applyState() {
        updateNotificationLabel(label, checkbox.checked)
        dayField.style.display = checkbox.checked ? '' : 'none'
    }

    document.getElementById('guest-toggle-track').addEventListener('click', () => {
        checkbox.checked = !checkbox.checked
        applyState()
    })
    checkbox.addEventListener('change', applyState)
}

function loadUserData() {
    const isGuest = new URLSearchParams(globalThis.location.search).get('guest') === 'true'
    if (isGuest) { showGuestForm(); return }

    $.ajax({
        url:   '/api/auth/me',
        type:  'GET',
        async: false,
        success: function (user) {
            currentUser = user
            SidebarManager.initialize()
            document.getElementById('username-input').value = user.username ?? ''
            document.getElementById('email-input').value    = user.email    ?? ''

            setupEditPasswordMode(user)
            setupNotificationSection(user)
            setupSocialLinkSection(user)

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
            document.getElementById('header-actions').appendChild(logoutBtn)

            setBreadcrumb([
                { i18nKey: 'myProfile', url: '/pages/views/UserView.html' },
                { i18nKey: 'edit' }
            ])
        },
        error: showGuestForm
    })
}

function setupEditPasswordMode(user) {
    document.getElementById('field-password').style.display         = 'none'
    document.getElementById('field-password-confirm').style.display = 'none'
    document.getElementById('field-change-password').style.display  = ''

    const btn = document.getElementById('password-edit-btn')
    if (!user.hasPassword) {
        btn.dataset.i18n = 'setPassword'
        btn.textContent  = I18n.t('setPassword')
    }
    btn.addEventListener('click', () => showPasswordChangeModal(user.id, user.hasPassword))
}

function setupNotificationSection(user) {
    const section = document.getElementById('notification-section')
    section.style.display = ''

    const checkbox = document.getElementById('notification-enabled-input')
    const daySelect = document.getElementById('notification-day-input')
    const label     = document.getElementById('notification-enabled-label')

    checkbox.checked = user.emailNotificationEnabled
    daySelect.value  = user.emailNotificationDay ?? 5
    updateNotificationLabel(label, checkbox.checked)

    const goalCheckbox = document.getElementById('goal-notification-enabled-input')
    const goalLabel    = document.getElementById('goal-notification-enabled-label')
    goalCheckbox.checked = user.goalEmailNotificationEnabled !== false
    updateNotificationLabel(goalLabel, goalCheckbox.checked)

    const langSelect = document.getElementById('language-select')
    langSelect.value = user.language ?? 'pt'

    document.querySelector('#notification-section .toggle-track').addEventListener('click', () => {
        checkbox.checked = !checkbox.checked
        updateNotificationLabel(label, checkbox.checked)
    })
    checkbox.addEventListener('change', () => updateNotificationLabel(label, checkbox.checked))

    document.getElementById('goal-notification-track').addEventListener('click', () => {
        goalCheckbox.checked = !goalCheckbox.checked
        updateNotificationLabel(goalLabel, goalCheckbox.checked)
    })
    goalCheckbox.addEventListener('change', () => updateNotificationLabel(goalLabel, goalCheckbox.checked))
}

function setupSocialLinkSection(user) {
    const section = document.getElementById('social-link-section')
    section.style.display = ''
    document.querySelector('.social-link-provider').insertAdjacentHTML('afterbegin', Icons.google(18))

    const btn    = document.getElementById('google-link-btn')
    const status = document.getElementById('google-link-status')

    if (user.googleLinked) {
        status.dataset.i18n = 'linked'
        status.textContent  = I18n.t('linked')
        status.classList.add('status-linked')
        btn.dataset.i18n = 'unlinkGoogle'
        btn.textContent  = I18n.t('unlinkGoogle')
        btn.addEventListener('click', () => {
            $.ajax({
                url: '/api/auth/link/google', type: 'DELETE', async: false,
                success: () => {
                    showToast(I18n.t('googleUnlinkedSuccess'), 'success')
                    globalThis.location.reload()
                },
                error: xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorUnlinkingGoogle'), 'error')
            })
        })
    } else {
        status.dataset.i18n = 'notLinked'
        status.textContent  = I18n.t('notLinked')
        status.classList.add('status-unlinked')
        btn.dataset.i18n = 'linkWithGoogle'
        btn.textContent  = I18n.t('linkWithGoogle')
        btn.addEventListener('click', () => {
            $.ajax({
                url: '/api/auth/link/google', type: 'POST', async: false,
                success: data => { globalThis.location.href = data.redirectUrl },
                error: xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorLinkingGoogle'), 'error')
            })
        })
    }

    const params = new URLSearchParams(globalThis.location.search)
    if (params.get('linked') === 'true') {
        showToast(I18n.t('googleLinkedSuccess'), 'success')
        history.replaceState({}, '', globalThis.location.pathname)
    } else if (params.get('link_error') === 'true') {
        showToast(I18n.t('googleLinkError'), 'error')
        history.replaceState({}, '', globalThis.location.pathname)
    }
}

function updateNotificationLabel(label, checked) {
    label.dataset.i18n = checked ? 'enabled' : 'disabled'
    label.textContent  = I18n.t(checked ? 'enabled' : 'disabled')
}

function showPasswordChangeModal(userId, hasPassword) {
    const overlay = document.createElement('div')
    overlay.className = 'modal-overlay'
    overlay.innerHTML = `
        <div class="modal-card">
            <p class="modal-title">${I18n.t(hasPassword ? 'changePassword' : 'setPassword')}</p>
            <div class="quick-add-fields">
                ${hasPassword ? `
                <div class="field">
                    <label>${I18n.t('currentPassword')} *</label>
                    <div class="pw-wrap">
                        <input type="password" id="modal-current-pw" placeholder="${I18n.t('currentPasswordPlaceholder')}" autocomplete="current-password">
                        <button class="pw-toggle" type="button" id="modal-current-pw-btn"></button>
                    </div>
                </div>` : ''}
                <div class="field">
                    <label>${I18n.t('newPassword')} *</label>
                    <div class="pw-wrap">
                        <input type="password" id="modal-new-pw" placeholder="${I18n.t('passwordPlaceholderNew')}" autocomplete="new-password">
                        <button class="pw-toggle" type="button" id="modal-new-pw-btn"></button>
                    </div>
                </div>
                <div class="field">
                    <label>${I18n.t('confirmPassword')} *</label>
                    <div class="pw-wrap">
                        <input type="password" id="modal-confirm-pw" placeholder="${I18n.t('confirmPasswordPlaceholder')}" autocomplete="new-password">
                        <button class="pw-toggle" type="button" id="modal-confirm-pw-btn"></button>
                    </div>
                </div>
            </div>
            <div class="modal-actions">
                <button class="btn btn-secondary" id="modal-pw-cancel">${I18n.t('commonCancel')}</button>
                <button class="btn btn-primary"   id="modal-pw-save">${I18n.t('commonSave')}</button>
            </div>
        </div>
    `
    document.body.appendChild(overlay)

    if (hasPassword) PasswordInput.setupToggle('modal-current-pw', 'modal-current-pw-btn')
    PasswordInput.setupToggle('modal-new-pw',     'modal-new-pw-btn')
    PasswordInput.setupToggle('modal-confirm-pw', 'modal-confirm-pw-btn')

    overlay.querySelector('#modal-pw-cancel').addEventListener('click', () => overlay.remove())
    overlay.addEventListener('click', e => { if (e.target === overlay) overlay.remove() })

    overlay.querySelector('#modal-pw-save').addEventListener('click', () => {
        const currentPw = hasPassword ? (overlay.querySelector('#modal-current-pw').value) : null
        const newPw     = overlay.querySelector('#modal-new-pw').value
        const confirmPw = overlay.querySelector('#modal-confirm-pw').value

        const missing = [
            hasPassword && !currentPw ? I18n.t('currentPassword') : null,
            newPw     ? null : I18n.t('newPassword'),
            confirmPw ? null : I18n.t('confirmPassword')
        ].filter(Boolean)

        if (missing.length > 0) {
            showToast(I18n.t('commonFillRequired', { fields: missing.join(', ') }), 'warning')
            return
        }

        $.ajax({
            url:         `/api/users/${userId}/password`,
            type:        'PUT',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify({ currentPassword: currentPw, newPassword: newPw, passwordConfirmation: confirmPw }),
            success: () => { overlay.remove(); showToast(I18n.t('passwordChangedSuccess'), 'success') },
            error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingUser'), 'error')
        })
    })
}

if (!globalThis.__appRouter) {
    await I18n.initialize()
    SidebarManager.initTranslations()
    I18n.onChange(() => SidebarManager.initTranslations())
    init()
}
