import { addDeleteIcon, navigate, showConfirm, showToast } from '../utils/FrontendFunctions.js'
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
        'username-input',
        'email-input',
        'password-input',
        'password-confirm-input'
    ])

    loadUserData()

    document.getElementById('cancel-btn').addEventListener('click', () =>
        navigate(currentUser ? '/pages/HomePage.html' : '/pages/Login.html')
    )

    document.getElementById('save-btn').addEventListener('click', function () {
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

        const isNewUser = !currentUser
        const body = {
            username:             document.getElementById('username-input').value,
            email:                document.getElementById('email-input').value,
            password:             document.getElementById('password-input').value,
            passwordConfirmation: document.getElementById('password-confirm-input').value
        }

        $.ajax({
            url:         isNewUser ? '/api/users' : `/api/users/${currentUser.id}`,
            type:        isNewUser ? 'POST' : 'PUT',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify(body),
            success:     function () {
                if (isNewUser) {
                    $.ajax({
                        url: '/api/auth/login',
                        type: 'POST',
                        async: false,
                        contentType: 'application/json',
                        data: JSON.stringify({ email: body.email, password: body.password }),
                        success: function () {
                            showToast(I18n.t('accountCreatedLoginSuccess'), 'success')
                            navigate('/pages/HomePage.html')
                        },
                        error: function () {
                            showToast(I18n.t('accountCreatedLoginFail'), 'warning')
                            navigate('/pages/Login.html')
                        }
                    })
                } else {
                    navigate('/pages/HomePage.html')
                }
            },
            error: function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingUser'), 'error') }
        })
    })
}

function loadUserData() {
    $.ajax({
        url:   '/api/auth/me',
        type:  'GET',
        async: false,
        success: function (user) {
            currentUser = user
            SidebarManager.initialize()
            document.getElementById('username-input').value = user.username ?? ''
            document.getElementById('email-input').value    = user.email    ?? ''

            const logoutBtn = document.createElement('button')
            logoutBtn.className = 'btn btn-ghost btn-sm'
            logoutBtn.type = 'button'
            logoutBtn.innerHTML = `${Icons.logout()}<span style="margin-left:5px" data-i18n="logout">${I18n.t('logout')}</span>`
            logoutBtn.addEventListener('click', () => {
                $.ajax({
                    url: '/api/auth/logout',
                    type: 'POST',
                    async: false,
                    complete: () => { globalThis.location.href = '/pages/Login.html' }
                })
            })
            document.getElementById('header-actions').appendChild(logoutBtn)

            const deleteBtn = addDeleteIcon()
            deleteBtn.addEventListener('click', function () {
                showConfirm(
                    I18n.t('deleteAccountConfirm'),
                    deleteUser,
                    I18n.t('deleteAccountTitle')
                )
            })
        },
        error: function () {
            ThemeManager.initialize()
            document.body.classList.add('user-guest')

            const main   = document.querySelector('.page-content')
            const header = document.createElement('div')
            header.className = 'guest-form-header'
            header.innerHTML = `<img src="../images/logo.png" alt="Finance Control"><h1 data-i18n="createAccount">${I18n.t('createAccount')}</h1><p data-i18n="guestFormHeader">${I18n.t('guestFormHeader')}</p>`
            main.insertBefore(header, main.firstChild)

            const titleEl = document.getElementById('page-title-text')
            if (titleEl) {
                titleEl.dataset.i18n = 'createAccount'
                titleEl.textContent  = I18n.t('createAccount')
            }
            const saveBtn = document.getElementById('save-btn')
            if (saveBtn) {
                saveBtn.dataset.i18n = 'createAccount'
                saveBtn.textContent  = I18n.t('createAccount')
            }
            const cancelBtn = document.getElementById('cancel-btn')
            if (cancelBtn) {
                cancelBtn.dataset.i18n = 'backToLogin'
                cancelBtn.textContent  = I18n.t('backToLogin')
            }
        }
    })
}

function deleteUser() {
    $.ajax({
        url:   `/api/users/${currentUser.id}`,
        type:  'DELETE',
        async: false,
        success: function () { globalThis.location.href = '/pages/Login.html' },
        error:   function (xhr) { showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingUser'), 'error') }
    })
}

if (!globalThis.__appRouter) init()
