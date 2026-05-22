import { showToast } from '../utils/FrontendFunctions.js'
import { InputMasks } from './utils/InputMasks.js'
import { PasswordInput } from './components/PasswordInput.js'
import { ThemeManager } from './ThemeManager.js'
import { I18n } from './i18n.js'
import { Icons } from './icons/IconLibrary.js'

ThemeManager.initialize()
document.querySelector('.btn-social-google').insertAdjacentHTML('afterbegin', Icons.google(18))
PasswordInput.setupToggle('password-input', 'password-img')

function applyTranslations() {
    document.querySelectorAll('[data-i18n]').forEach(el => {
        el.textContent = I18n.t(el.dataset.i18n)
    })
    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
        el.placeholder = I18n.t(el.dataset.i18nPlaceholder)
    })
}

await I18n.initialize()
applyTranslations()
InputMasks.autoInit()
I18n.onChange(applyTranslations)

let lastIdentifier = ''

document.getElementById('register-btn').addEventListener('click', function () {
    globalThis.location.href = '/pages/crud/User.html?guest=true'
})

document.getElementById('login-btn').addEventListener('click', function () {
    const identifier = document.getElementById('email-input').value.trim()
    const password   = document.getElementById('password-input').value

    if (!identifier || !password) {
        showToast(I18n.t('emptyEmailPassword'), 'warning')
        return
    }

    lastIdentifier = identifier

    $.ajax({
        url: '/api/auth/login',
        type: 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify({ identifier, password }),
        success: function () {
            document.getElementById('email-not-verified-alert').style.display = 'none'
            $.ajax({
                url: '/api/auth/me', type: 'GET', async: false,
                success: function (user) { I18n.setLanguage(user.language || 'pt') }
            })
            globalThis.location.href = '/pages/AppShell.html'
        },
        error: function (xhr) {
            const code = xhr.responseJSON?.errorCode ?? ''
            const msg  = xhr.responseJSON?.message  ?? ''
            if (xhr.status === 401 && code === 'error.auth.emailNotVerified') {
                document.getElementById('email-not-verified-alert').style.display = ''
            } else if (xhr.status === 401) {
                showToast(msg || I18n.t('errorLoginInvalid'), 'error')
            } else {
                showToast(msg || I18n.t('errorLogin'), 'error')
            }
        }
    })
})

document.getElementById('resend-verification-btn').addEventListener('click', function () {
    const email = lastIdentifier || document.getElementById('email-input').value.trim()
    if (!email) {
        showToast(I18n.t('emptyEmailPassword'), 'warning')
        return
    }

    $.ajax({
        url: `/api/auth/resend-verification?email=${encodeURIComponent(email)}`,
        type: 'POST',
        async: false,
        success: function () {
            showToast(I18n.t('verificationEmailSent'), 'success')
        },
        error: function () {
            showToast(I18n.t('errorResendVerification'), 'error')
        }
    })
})
