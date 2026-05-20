import { showToast } from '../utils/FrontendFunctions.js'
import { InputMasks } from './utils/InputMasks.js'
import { PasswordInput } from './components/PasswordInput.js'
import { ThemeManager } from './ThemeManager.js'
import { I18n } from './i18n.js'

ThemeManager.initialize()
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

    $.ajax({
        url: '/api/auth/login',
        type: 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify({ identifier, password }),
        success: function () {
            $.ajax({
                url: '/api/auth/me', type: 'GET', async: false,
                success: function (user) { I18n.setLanguage(user.language || 'pt') }
            })
            globalThis.location.href = '/pages/AppShell.html'
        },
        error: function (xhr) {
            if (xhr.status === 401)
                showToast(xhr.responseJSON?.message ?? I18n.t('errorLoginInvalid'), 'error')
            else
                showToast(xhr.responseJSON?.message ?? I18n.t('errorLogin'), 'error')
        }
    })
})
