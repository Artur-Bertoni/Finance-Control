import { clearDirtyGuard, navigate, setBreadcrumb, setupDirtyGuard, showToast } from '../utils/FrontendFunctions.js'
import { PasswordInput } from './components/PasswordInput.js'
import { SidebarManager } from './components/SidebarManager.js'
import { ThemeManager } from './ThemeManager.js'
import { Icons } from './icons/IconLibrary.js'
import { setupRequiredFieldValidation, validateRequiredFields } from './utils/FieldValidation.js'
import { I18n } from './i18n.js'

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
        navigate(currentUser ? '/pages/UserView.html' : '/pages/Login.html')
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
            showToast(I18n.t('fillRequiredFields', { fields: emptyFields.join(', ') }), 'warning')
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
            showToast(I18n.t('fillRequiredFields', { fields: emptyFields.join(', ') }), 'warning')
            return
        }

        const selectedLanguage = document.getElementById('language-select').value
        $.ajax({
            url:         `/api/users/${currentUser.id}`,
            type:        'PUT',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify({
                username:                   document.getElementById('username-input').value,
                email:                      document.getElementById('email-input').value,
                emailNotificationEnabled:   document.getElementById('notification-enabled-input').checked,
                emailNotificationDay:       Number.parseInt(document.getElementById('notification-day-input').value, 10),
                language:                   selectedLanguage
            }),
            success: function () {
                clearDirtyGuard()
                I18n.setLanguage(selectedLanguage)
                navigate('/pages/UserView.html')
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

            setupEditPasswordMode(user.id)
            setupNotificationSection(user)

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
                { i18nKey: 'myProfile', url: '/pages/UserView.html' },
                { i18nKey: 'edit' }
            ])
        },
        error: showGuestForm
    })
}

function setupEditPasswordMode(userId) {
    document.getElementById('field-password').style.display         = 'none'
    document.getElementById('field-password-confirm').style.display = 'none'
    document.getElementById('field-change-password').style.display  = ''
    document.getElementById('password-edit-btn').addEventListener('click', () => showPasswordChangeModal(userId))
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

    const langSelect = document.getElementById('language-select')
    langSelect.value = user.language ?? 'pt'

    // clicking the track toggles the checkbox
    document.querySelector('.toggle-track').addEventListener('click', () => {
        checkbox.checked = !checkbox.checked
        updateNotificationLabel(label, checkbox.checked)
    })
    checkbox.addEventListener('change', () => updateNotificationLabel(label, checkbox.checked))
}

function updateNotificationLabel(label, checked) {
    label.dataset.i18n = checked ? 'enabled' : 'disabled'
    label.textContent  = I18n.t(checked ? 'enabled' : 'disabled')
}

function showPasswordChangeModal(userId) {
    const overlay = document.createElement('div')
    overlay.className = 'modal-overlay'
    overlay.innerHTML = `
        <div class="modal-card">
            <p class="modal-title">${I18n.t('changePassword')}</p>
            <div class="quick-add-fields">
                <div class="field">
                    <label>${I18n.t('currentPassword')} *</label>
                    <div class="pw-wrap">
                        <input type="password" id="modal-current-pw" placeholder="${I18n.t('currentPasswordPlaceholder')}" autocomplete="current-password">
                        <button class="pw-toggle" type="button" id="modal-current-pw-btn"></button>
                    </div>
                </div>
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
                <button class="btn btn-secondary" id="modal-pw-cancel">${I18n.t('cancel')}</button>
                <button class="btn btn-primary"   id="modal-pw-save">${I18n.t('save')}</button>
            </div>
        </div>
    `
    document.body.appendChild(overlay)

    PasswordInput.setupToggle('modal-current-pw', 'modal-current-pw-btn')
    PasswordInput.setupToggle('modal-new-pw',      'modal-new-pw-btn')
    PasswordInput.setupToggle('modal-confirm-pw',  'modal-confirm-pw-btn')

    overlay.querySelector('#modal-pw-cancel').addEventListener('click', () => overlay.remove())
    overlay.addEventListener('click', e => { if (e.target === overlay) overlay.remove() })

    overlay.querySelector('#modal-pw-save').addEventListener('click', () => {
        const currentPw = overlay.querySelector('#modal-current-pw').value
        const newPw     = overlay.querySelector('#modal-new-pw').value
        const confirmPw = overlay.querySelector('#modal-confirm-pw').value

        const missing = [
            currentPw ? null : I18n.t('currentPassword'),
            newPw     ? null : I18n.t('newPassword'),
            confirmPw ? null : I18n.t('confirmPassword')
        ].filter(Boolean)

        if (missing.length > 0) {
            showToast(I18n.t('fillRequiredFields', { fields: missing.join(', ') }), 'warning')
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
