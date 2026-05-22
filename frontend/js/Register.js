import { showToast } from '../utils/FrontendFunctions.js'
import { InputMasks } from './utils/InputMasks.js'
import { PasswordInput } from './components/PasswordInput.js'
import { ThemeManager } from './ThemeManager.js'
import { I18n } from './i18n.js'

ThemeManager.initialize()
ThemeManager.updateToggleIcon()
PasswordInput.setupToggle('password-input', 'password-img')
PasswordInput.setupToggle('password-confirm-input', 'password-confirm-img')

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
ThemeManager.updateToggleIcon()
I18n.onChange(applyTranslations)
I18n.onChange(() => ThemeManager.updateToggleIcon())
I18n.onChange(() => updateLangSwitcher())

document.getElementById('theme-toggle-btn').addEventListener('click', ThemeManager.toggle)

const FLAG_CODES = { pt: 'fi-br', en: 'fi-us', es: 'fi-es' }
const langTrigger = document.getElementById('lang-trigger')
const langMenu    = document.getElementById('lang-menu')

function updateLangSwitcher() {
    const lang = I18n.getLanguage()
    langTrigger.innerHTML = `<span class="fi fi-squared ${FLAG_CODES[lang] ?? 'fi-br'}"></span>`
    langMenu.querySelectorAll('.login-lang-option').forEach(btn =>
        btn.classList.toggle('active', btn.dataset.lang === lang)
    )
}

langTrigger.addEventListener('click', e => { e.stopPropagation(); langMenu.classList.toggle('show') })
langMenu.querySelectorAll('.login-lang-option').forEach(btn =>
    btn.addEventListener('click', () => { I18n.setLanguage(btn.dataset.lang); langMenu.classList.remove('show') })
)
document.addEventListener('click', () => langMenu.classList.remove('show'))
updateLangSwitcher()

document.getElementById('back-btn').addEventListener('click', () => {
    globalThis.location.href = '/pages/Login.html'
})

document.getElementById('register-btn').addEventListener('click', () => {
    const username        = document.getElementById('username-input').value.trim()
    const email           = document.getElementById('email-input').value.trim()
    const password        = document.getElementById('password-input').value
    const passwordConfirm = document.getElementById('password-confirm-input').value

    const missing = [
        username        ? null : I18n.t('username'),
        email           ? null : I18n.t('emailAddress'),
        password        ? null : I18n.t('password'),
        passwordConfirm ? null : I18n.t('confirmPassword'),
    ].filter(Boolean)

    if (missing.length > 0) {
        showToast(I18n.t('fillRequiredFields', { fields: missing.join(', ') }), 'warning')
        return
    }

    const body = {
        username,
        email,
        password,
        passwordConfirmation: passwordConfirm,
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
                    sessionStorage.setItem('showEmailVerificationNotice', 'true')
                    showToast(I18n.t('accountCreatedLoginSuccess'), 'success')
                    globalThis.location.href = '/pages/AppShell.html'
                },
                error: function () {
                    showToast(I18n.t('accountCreatedLoginFail'), 'warning')
                    globalThis.location.href = '/pages/Login.html'
                },
            })
        },
        error: function (xhr) {
            showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingUser'), 'error')
        },
    })
})
