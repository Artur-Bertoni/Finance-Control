import { I18n } from '../js/i18n.js'
import { FinnySvg } from '../js/utils/FinnySvg.js'
export { showConfirm, showConfirmAsync } from '../js/modals/ConfirmModal.js'
export { showQuickAdd } from '../js/modals/QuickAddModal.js'

const FINNY_FACE_SVG = FinnySvg.faceSvg('toast-finny')

const CURRENCY_LOCALE_MAP = { pt: 'pt-BR', en: 'en-US', es: 'es-ES' }
const CURRENCY_SYMBOL_MAP = { pt: 'R$', en: '$', es: '$' }

export function formatCurrency(value) {
    const locale = CURRENCY_LOCALE_MAP[I18n.getLanguage()] ?? 'pt-BR'
    return new Intl.NumberFormat(locale, { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value)
}

export function currencySymbol() {
    return CURRENCY_SYMBOL_MAP[I18n.getLanguage()] ?? '$'
}

export function formatMoney(value) {
    return `${currencySymbol()} ${formatCurrency(value)}`
}

export function formatDate(isoDateStr) {
    if (!isoDateStr) return ''
    const [y, m, d] = isoDateStr.split('-')
    return I18n.getLanguage() === 'en' ? `${m}/${d}/${y}` : `${d}/${m}/${y}`
}

export function formatDateTime(isoStr) {
    if (!isoStr) return ''
    const hasZone = isoStr.endsWith('Z') || /[+-]\d{2}:\d{2}$/.test(isoStr)
    const dt  = new Date(hasZone ? isoStr : isoStr + 'Z')
    const pad = n => String(n).padStart(2, '0')
    const day = pad(dt.getDate()), mo = pad(dt.getMonth() + 1), y = dt.getFullYear()
    const time = `${pad(dt.getHours())}:${pad(dt.getMinutes())}`
    return I18n.getLanguage() === 'en' ? `${mo}/${day}/${y} - ${time}` : `${day}/${mo}/${y} - ${time}`
}

let _emailVerifToastAt = 0

$.ajaxSetup({
    beforeSend(xhr, settings) {
        xhr.setRequestHeader('Accept-Language', I18n.getLanguage())

        const method = (settings.type || 'GET').toUpperCase()
        const isWrite = ['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)
        const url = settings.url || ''
        const isExempt = /\/(api\/auth|api\/users|api\/notifications)/.test(url)

        if (isWrite && !isExempt && globalThis.__currentUser?.emailVerified === false) {
            const now = Date.now()
            if (now - _emailVerifToastAt > 4000) {
                _emailVerifToastAt = now
                showToast(I18n.t('emailVerificationRequired'), 'warning', null, { saveToHistory: false })
            }
            return false
        }
    }
})

export function navigate(url) {
    if (globalThis.__appRouter?.navigate && !url.includes('Login.html')) {
        globalThis.__appRouter.navigate(url)
    } else {
        globalThis.location.href = url
    }
}

export function navigateWithToast(url, message, type = 'success', viewLink = null) {
    sessionStorage.setItem('pendingToast', JSON.stringify({ message, type, link: viewLink ?? url }))
    if (globalThis.__appRouter?.navigate && !url.includes('Login.html')) {
        globalThis.__appRouter.navigate(url)
    } else {
        globalThis.location.href = url
    }
}

export function showPendingToast() {
    const pending = sessionStorage.getItem('pendingToast')
    if (pending) {
        const { message, type, link } = JSON.parse(pending)
        sessionStorage.removeItem('pendingToast')
        const action = link ? { label: I18n.t('view'), url: link } : null
        setTimeout(() => showToast(message, type, action), 100)
    }
}

const NOTIF_TYPE_META = {
    GOAL_MILESTONE_50:     { icon: '📊', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 50 }, toastType: 'info'    },
    GOAL_MILESTONE_75:     { icon: '📈', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 75 }, toastType: 'info'    },
    GOAL_MILESTONE_90:     { icon: '🔥', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 90 }, toastType: 'warning' },
    GOAL_COMPLETED:        { icon: '🎯', i18nKey: 'notifGoalCompleted',   toastType: 'success' },
    GOAL_EXCEEDED:         { icon: '⚠️', i18nKey: 'notifGoalExceeded',   toastType: 'warning' },
    GOAL_DEADLINE_WARNING: { icon: '⏰', i18nKey: 'notifGoalDeadline',   toastType: 'warning' },
}

export function showPendingNotifications() {
    const raw = sessionStorage.getItem('pendingNotifications')
    if (!raw) return
    sessionStorage.removeItem('pendingNotifications')
    const notifications = JSON.parse(raw)
    notifications.forEach((n, i) => {
        const meta  = NOTIF_TYPE_META[n.type] ?? { icon: '🔔', i18nKey: 'notifications', toastType: 'info' }
        const label = I18n.t(meta.i18nKey, meta.i18nParams)
        const msg   = `${meta.icon} ${label}: ${n.goalName ?? ''}`
        setTimeout(() => showToast(msg, meta.toastType, { label: I18n.t('viewGoal'), url: n.link }, { saveToHistory: false }), 700 + i * 600)
    })
}

export function doRequest(url, httpMethod = 'GET', body = null) {
    let result = null
    $.ajax({
        url,
        type: httpMethod,
        async: false,
        contentType: 'application/json',
        data: body === null ? undefined : JSON.stringify(body),
        success: function (response) { result = response },
    })
    return result
}

const TOAST_DURATION = 4500

const FINNY_HISTORY_KEY = '__finny_history'
const FINNY_HISTORY_MAX = 200

export function saveToFinnyHistory(message, type = 'info', link = null) {
    try {
        $.ajax({
            url: '/api/notifications/history',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ message, severity: type, link: link ?? null }),
            async: true,
            error: () => {}
        })
    } catch { }
}

export function isWindowActive() {
    return document.visibilityState !== 'hidden' && document.hasFocus()
}

export function showToast(message, type = 'info', action = null, { saveToHistory = true, longDuration = false } = {}) {
    if (saveToHistory) saveToFinnyHistory(message, type, action?.url ?? null)
    let container = document.getElementById('toast-container')
    if (!container) {
        container = document.createElement('div')
        container.id = 'toast-container'
        container.className = 'toast-container'
        document.body.appendChild(container)
    }

    const duration = longDuration ? TOAST_DURATION * 2 : TOAST_DURATION

    const toast = document.createElement('div')
    toast.className = `toast ${type}${action ? ' toast--clickable' : ''}`
    toast.innerHTML = `
        <span class="toast-icon">${FINNY_FACE_SVG}</span>
        <div class="toast-body">
            <span class="toast-text">${message}</span>
            ${action ? `<span class="toast-click-hint">(${I18n.t('clickToView')})</span>` : ''}
        </div>
        <button class="toast-close" aria-label="${I18n.t('close')}">×</button>
        <div class="toast-progress-bar"></div>
    `

    if (action) {
        toast.addEventListener('click', e => {
            if (e.target.closest('.toast-close')) return
            clearTimeout(timerId)
            cancelAnimationFrame(rafId)
            dismissToast(toast)
            navigate(action.url)
        })
    }

    const progressBar = toast.querySelector('.toast-progress-bar')
    let elapsed = 0
    let lastTick = 0
    let paused = false
    let timerId = null
    let rafId = null

    function tick() {
        if (paused) return
        const now = performance.now()
        elapsed = Math.min(elapsed + (now - lastTick), duration)
        lastTick = now
        progressBar.style.width = `${(1 - elapsed / duration) * 100}%`
        if (elapsed < duration) rafId = requestAnimationFrame(tick)
    }

    function start() {
        if (!isWindowActive()) { paused = true; return }
        paused = false
        lastTick = performance.now()
        timerId = setTimeout(() => { cancelAnimationFrame(rafId); dismissToast(toast) }, duration - elapsed)
        rafId = requestAnimationFrame(tick)
    }

    function pause() {
        if (paused) return
        paused = true
        elapsed = Math.min(elapsed + (performance.now() - lastTick), duration)
        clearTimeout(timerId)
        cancelAnimationFrame(rafId)
    }

    function onFocusChange() {
        if (isWindowActive()) start()
        else pause()
    }
    window.addEventListener('focus', onFocusChange)
    window.addEventListener('blur', onFocusChange)
    document.addEventListener('visibilitychange', onFocusChange)
    toast._cleanup = () => {
        window.removeEventListener('focus', onFocusChange)
        window.removeEventListener('blur', onFocusChange)
        document.removeEventListener('visibilitychange', onFocusChange)
    }

    toast.querySelector('.toast-close').addEventListener('click', () => {
        clearTimeout(timerId)
        cancelAnimationFrame(rafId)
        dismissToast(toast)
    })
    toast.addEventListener('mouseenter', pause)
    toast.addEventListener('mouseleave', start)

    container.appendChild(toast)
    start()
}

function dismissToast(toast) {
    if (toast.classList.contains('toast-closing')) return
    toast._cleanup?.()
    toast.classList.add('toast-closing')
    setTimeout(() => toast.remove(), 260)
}

export function setupDirtyGuard() {
    let dirty = false
    globalThis.__dirtyGuard = () => dirty
    const form = document.querySelector('.form-card')
    if (!form) return
    form.addEventListener('change', () => { dirty = true })
    form.addEventListener('input',  () => { dirty = true })
}

export function clearDirtyGuard() {
    globalThis.__dirtyGuard = null
}

export function addDeleteIcon() {
    const btn = document.createElement('button')
    btn.className = 'btn btn-danger btn-sm'
    btn.id = 'delete-btn'
    btn.type = 'button'
    btn.dataset.i18n = 'delete'
    btn.textContent = I18n.t('delete')

    const container = document.getElementById('header-actions')
    if (container) container.appendChild(btn)

    return btn
}

let _breadcrumbCrumbs = []

export function setBreadcrumb(crumbs) {
    _breadcrumbCrumbs = crumbs
    globalThis.__currentBreadcrumbs = crumbs
    _renderBreadcrumb()
    const btn = document.getElementById('back-btn')
    if (btn) btn.style.display = crumbs.length >= 2 ? '' : 'none'
}

export function rerenderBreadcrumb() {
    _renderBreadcrumb()
}

function _renderBreadcrumb() {
    const nav = document.getElementById('breadcrumb')
    if (!nav || _breadcrumbCrumbs.length === 0) return

    const titleEl = document.getElementById('page-title-text')
    nav.innerHTML = ''

    _breadcrumbCrumbs.forEach((crumb, i) => {
        const isLast = i === _breadcrumbCrumbs.length - 1
        const text   = crumb.i18nKey ? I18n.t(crumb.i18nKey) : (crumb.label ?? '')

        if (isLast) {
            const el = titleEl ?? Object.assign(document.createElement('span'), { id: 'page-title-text', className: 'page-title' })
            el.textContent = text
            delete el.dataset.i18n
            nav.appendChild(el)
        } else {
            const a = document.createElement('a')
            a.className = 'breadcrumb-link'
            a.textContent = text
            if (crumb.url) a.href = crumb.url
            nav.appendChild(a)

            const sep = document.createElement('span')
            sep.className = 'breadcrumb-sep'
            sep.setAttribute('aria-hidden', 'true')
            sep.textContent = '›'
            nav.appendChild(sep)
        }
    })
}

export function createAddCard(label, url) {
    const card = document.createElement('div')
    card.className = 'item-card item-card--add'
    card.innerHTML = `<div class="item-card--add-inner"><i class="ph ph-plus-circle"></i><span>${label}</span></div>`
    card.addEventListener('click', () => navigate(url))
    return card
}

export function initFilterToggle(isActiveNow) {
    const section = document.querySelector('#view .filter-section') ?? document.querySelector('.filter-section')
    if (!section) return

    const btn = section.querySelector('.filter-toggle-btn')

    const hintSpan = document.createElement('span')
    hintSpan.className = 'filter-toggle-hint'
    hintSpan.textContent = I18n.t('filterClickHint')
    const caret = btn?.querySelector('.filter-toggle-caret')
    if (caret) caret.before(hintSpan)

    const setOpen = (open) => {
        section.classList.toggle('open', open)
        btn?.setAttribute('aria-expanded', String(open))
    }

    const syncActive = () => {
        section.classList.toggle('filter-active', isActiveNow())
    }

    btn?.addEventListener('click', () => {
        const willOpen = !section.classList.contains('open')
        setOpen(willOpen)
    })

    syncActive()
    setOpen(isActiveNow())

    return { syncActive }
}

export function setupSearch(onInput, onClear, syncFn) {
    const input    = document.querySelector('#view #search-input')    ?? document.getElementById('search-input')
    const clearBtn = document.querySelector('#view #clear-search-btn') ?? document.getElementById('clear-search-btn')

    const defaultSync = () => {
        const wrapper = clearBtn?.closest('.filter-clear-field') ?? clearBtn
        if (wrapper) wrapper.style.display = input?.value ? 'flex' : 'none'
    }
    const sync = syncFn ?? defaultSync

    input?.addEventListener('input', () => { onInput(input.value); sync() })
    clearBtn?.addEventListener('click', () => {
        if (input) input.value = ''
        onClear?.()
        sync()
    })
    sync()
}

export function selectOptionByText(selectId, text) {
    for (const opt of document.getElementById(selectId)?.options ?? []) {
        if (opt.innerText === text) { opt.selected = true; break }
    }
}

export function addOptionToSelect(selectIds, value, label) {
    const ids = Array.isArray(selectIds) ? selectIds : [selectIds]
    for (const id of ids) {
        const sel = document.getElementById(id)
        if (!sel) continue
        const opt = document.createElement('option')
        opt.value = value
        opt.text  = label
        sel.appendChild(opt)
        sel.value = value
    }
}

export function populateSelect(elementId, apiUrl, iconKeyField = null) {
    const raw  = doRequest(apiUrl, 'GET') ?? []
    const list = document.getElementById(elementId)
    if (!list) return
    for (const item of raw) {
        const opt = document.createElement('option')
        opt.value     = item.id
        opt.innerText = item.name
        if (iconKeyField && item[iconKeyField]) opt.dataset.iconKey = item[iconKeyField]
        list.appendChild(opt)
    }
}

