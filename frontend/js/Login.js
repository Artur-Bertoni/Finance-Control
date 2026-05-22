import { showToast } from '../utils/FrontendFunctions.js'
import { InputMasks } from './utils/InputMasks.js'
import { PasswordInput } from './components/PasswordInput.js'
import { ThemeManager } from './ThemeManager.js'
import { I18n } from './i18n.js'
import { Icons } from './icons/IconLibrary.js'

ThemeManager.initialize()
ThemeManager.updateToggleIcon()
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

document.getElementById('register-btn').addEventListener('click', function () {
    globalThis.location.href = '/pages/Register.html'
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
            const msg = xhr.responseJSON?.message ?? ''
            showToast(msg || I18n.t(xhr.status === 401 ? 'errorLoginInvalid' : 'errorLogin'), 'error')
        }
    })
})
