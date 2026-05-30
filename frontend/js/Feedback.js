import { I18n }      from './i18n.js'
import { SidebarManager } from './components/SidebarManager.js'
import { setBreadcrumb, doRequest, showToast } from '../utils/FrontendFunctions.js'

let _selectedNps = null
let _npsSkipped  = false

export async function init() {
    await SidebarManager.initialize()
    setBreadcrumb([{ label: I18n.t('feedback'), url: '/pages/Feedback.html' }])

    _renderNpsButtons()
    _bindEvents()
    I18n.onChange(_renderNpsButtons)
}

function _renderNpsButtons() {
    const container = document.getElementById('nps-buttons')
    if (!container) return

    container.innerHTML = ''
    for (let i = 0; i <= 10; i++) {
        const btn = document.createElement('button')
        btn.type      = 'button'
        btn.textContent = String(i)
        btn.className = 'feedback-nps-btn' + (i === _selectedNps ? ' feedback-nps-btn--selected' : '')
        btn.dataset.value = i
        btn.addEventListener('click', () => _selectNps(i))
        container.appendChild(btn)
    }
}

function _selectNps(value) {
    _selectedNps = value
    _npsSkipped  = false
    document.querySelectorAll('.feedback-nps-btn').forEach(b => {
        b.classList.toggle('feedback-nps-btn--selected', Number(b.dataset.value) === value)
    })
    const skipBtn = document.getElementById('nps-skip-btn')
    if (skipBtn) skipBtn.classList.remove('active')
}

function _bindEvents() {
    document.getElementById('nps-skip-btn')?.addEventListener('click', () => {
        _selectedNps = null
        _npsSkipped  = true
        document.querySelectorAll('.feedback-nps-btn').forEach(b => b.classList.remove('feedback-nps-btn--selected'))
        const skipBtn = document.getElementById('nps-skip-btn')
        if (skipBtn) skipBtn.classList.add('active')
    })

    document.getElementById('feedback-submit-btn')?.addEventListener('click', _submit)
}

function _submit() {
    const typeEl    = document.querySelector('input[name="feedbackType"]:checked')
    const messageEl = document.getElementById('feedback-message')
    const typeError = document.getElementById('type-error')
    const msgError  = document.getElementById('message-error')

    let valid = true

    typeError.hidden = true
    msgError.hidden  = true

    if (!typeEl) {
        typeError.textContent = I18n.t('feedbackTypeRequired')
        typeError.hidden      = false
        valid = false
    }

    const message = messageEl?.value?.trim() ?? ''
    if (message.length < 10) {
        msgError.textContent = I18n.t('feedbackMinLength')
        msgError.hidden      = false
        valid = false
    }

    if (!valid) return

    const body = {
        type:     typeEl.value,
        message,
        npsScore: _npsSkipped ? null : _selectedNps,
    }

    const result = doRequest('/api/feedback', 'POST', body)
    if (result === null) {
        showToast(I18n.t('feedbackError'), 'error')
        return
    }
    showToast(I18n.t('feedbackSuccess'), 'success')
    _resetForm()
}

function _resetForm() {
    document.querySelectorAll('input[name="feedbackType"]').forEach(r => (r.checked = false))
    const msgEl = document.getElementById('feedback-message')
    if (msgEl) msgEl.value = ''
    _selectedNps = null
    _npsSkipped  = false
    document.querySelectorAll('.feedback-nps-btn').forEach(b => b.classList.remove('feedback-nps-btn--selected'))
    const skipBtn = document.getElementById('nps-skip-btn')
    if (skipBtn) skipBtn.classList.remove('active')
}
