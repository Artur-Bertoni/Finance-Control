import { Icons } from '../js/icons/IconLibrary.js'
import { I18n } from '../js/i18n.js'

// Send current language with every AJAX request so the backend can respond in the right locale
$.ajaxSetup({
    beforeSend(xhr) {
        xhr.setRequestHeader('Accept-Language', I18n.getLanguage())
    }
})

export function navigate(url) {
    if (globalThis.__appRouter?.navigate && !url.includes('Login.html')) {
        globalThis.__appRouter.navigate(url)
    } else {
        globalThis.location.href = url
    }
}

export function navigateWithToast(url, message, type = 'success') {
    sessionStorage.setItem('pendingToast', JSON.stringify({ message, type }))
    if (globalThis.__appRouter?.navigate && !url.includes('Login.html')) {
        globalThis.__appRouter.navigate(url)
    } else {
        globalThis.location.href = url
    }
}

export function showPendingToast() {
    const pending = sessionStorage.getItem('pendingToast')
    if (pending) {
        const { message, type } = JSON.parse(pending)
        sessionStorage.removeItem('pendingToast')
        setTimeout(() => showToast(message, type), 100)
    }
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
        error: function (xhr) { console.error('Request error:', xhr.status, xhr.responseText) }
    })
    return result
}

const TOAST_DURATION = 4500

export function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container')
    if (!container) {
        container = document.createElement('div')
        container.id = 'toast-container'
        container.className = 'toast-container'
        document.body.appendChild(container)
    }

    const icons = {
        success: Icons.check(),
        error:   Icons.error(),
        warning: Icons.warning(),
        info:    Icons.info()
    }

    const toast = document.createElement('div')
    toast.className = `toast ${type}`
    toast.innerHTML = `
        <span class="toast-icon">${icons[type] || icons.info}</span>
        <span class="toast-text">${message}</span>
        <button class="toast-close" aria-label="${I18n.t('close')}">×</button>
        <div class="toast-progress-bar"></div>
    `

    const progressBar = toast.querySelector('.toast-progress-bar')
    let elapsed = 0
    let lastTick = 0
    let paused = false
    let timerId = null
    let rafId = null

    function tick() {
        if (paused) return
        const now = performance.now()
        elapsed = Math.min(elapsed + (now - lastTick), TOAST_DURATION)
        lastTick = now
        progressBar.style.width = `${(1 - elapsed / TOAST_DURATION) * 100}%`
        if (elapsed < TOAST_DURATION) rafId = requestAnimationFrame(tick)
    }

    function start() {
        paused = false
        lastTick = performance.now()
        timerId = setTimeout(() => { cancelAnimationFrame(rafId); dismissToast(toast) }, TOAST_DURATION - elapsed)
        rafId = requestAnimationFrame(tick)
    }

    function pause() {
        if (paused) return
        paused = true
        elapsed = Math.min(elapsed + (performance.now() - lastTick), TOAST_DURATION)
        clearTimeout(timerId)
        cancelAnimationFrame(rafId)
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
    toast.classList.add('toast-closing')
    setTimeout(() => toast.remove(), 260)
}

export function showConfirm(message, onConfirm, title = null) {
    const overlay = document.createElement('div')
    overlay.className = 'modal-overlay'
    overlay.innerHTML = `
        <div class="modal-card">
            <p class="modal-title">${title ?? I18n.t('confirmAction')}</p>
            <p class="modal-message">${message}</p>
            <div class="modal-actions">
                <button class="btn btn-secondary" id="modal-cancel-btn">${I18n.t('cancel')}</button>
                <button class="btn btn-danger"    id="modal-confirm-btn">${I18n.t('confirm')}</button>
            </div>
        </div>
    `
    document.body.appendChild(overlay)

    overlay.querySelector('#modal-cancel-btn').addEventListener('click', () => overlay.remove())
    overlay.querySelector('#modal-confirm-btn').addEventListener('click', () => {
        overlay.remove()
        onConfirm()
    })
    overlay.addEventListener('click', (e) => { if (e.target === overlay) overlay.remove() })
}

export function addDeleteIcon() {
    const btn = document.createElement('button')
    btn.className = 'btn btn-danger btn-sm'
    btn.id = 'delete-btn'
    btn.type = 'button'
    btn.innerHTML = `${Icons.delete()} ${I18n.t('delete')}`

    const container = document.getElementById('header-actions')
    if (container) container.appendChild(btn)

    return btn
}

export function showQuickAdd({ title, fields, apiUrl, buildBody, onSuccess }) {
    const overlay = document.createElement('div')
    overlay.className = 'modal-overlay'

    const renderInput = (f) => {
        if (f.type === 'textarea')
            return `<textarea id="qaf-${f.id}" placeholder="${f.placeholder ?? ''}"></textarea>`
        if (f.type === 'select') {
            const opts = (f.options ?? []).map(o => `<option value="${o.value}">${o.label}</option>`).join('')
            const sel  = `<select id="qaf-${f.id}"><option value="">— ${f.placeholder ?? I18n.t('selectAccount')} —</option>${opts}</select>`
            if (!f.addBtn) return sel
            return (
                '<div class="select-with-add">' + sel +
                '<button type="button" class="btn-add-inline" id="qaf-' + f.id + '-add-btn" title="' + (f.addBtn.title ?? '') + '">' + Icons.add() + '</button>' +
                '</div>'
            )
        }
        const stepAttr = f.step ? ' step="' + f.step + '"' : ''
        return '<input id="qaf-' + f.id + '" type="' + (f.type ?? 'text') + '" placeholder="' + (f.placeholder ?? '') + '"' + stepAttr + '>'
    }

    const fieldsHtml = fields.map(f => {
        const inputHtml = renderInput(f)
        return '<div class="field"><label>' + f.label + '</label>' + inputHtml + '</div>'
    }).join('')

    overlay.innerHTML = `
        <div class="modal-card quick-add-card">
            <p class="modal-title">${title}</p>
            <div class="quick-add-fields">${fieldsHtml}</div>
            <div class="modal-actions">
                <button class="btn btn-secondary" id="qa-cancel">${I18n.t('cancel')}</button>
                <button class="btn btn-primary"   id="qa-save">${I18n.t('save')}</button>
            </div>
        </div>
    `
    document.body.appendChild(overlay)

    fields.filter(f => f.type === 'select' && f.addBtn).forEach(f => {
        const addBtn = overlay.querySelector('#qaf-' + f.id + '-add-btn')
        if (!addBtn) return
        addBtn.addEventListener('click', () => {
            showQuickAdd({
                title:     f.addBtn.title  ?? '',
                apiUrl:    f.addBtn.apiUrl,
                fields:    f.addBtn.fields,
                buildBody: f.addBtn.buildBody,
                onSuccess: item => {
                    const sel = overlay.querySelector('#qaf-' + f.id)
                    const opt = document.createElement('option')
                    opt.value = item.id
                    opt.text  = item.name
                    sel.appendChild(opt)
                    sel.value = item.id
                    if (f.addBtn.onSuccess) f.addBtn.onSuccess(item)
                }
            })
        })
    })

    overlay.querySelector('#qa-cancel').addEventListener('click', () => overlay.remove())
    overlay.addEventListener('click', e => { if (e.target === overlay) overlay.remove() })

    overlay.querySelector('#qa-save').addEventListener('click', () => {
        const values = {}
        fields.forEach(f => { values[f.id] = overlay.querySelector('#qaf-' + f.id)?.value ?? '' })

        const missing = fields.filter(f => f.required && !values[f.id])
        if (missing.length) {
            showToast(I18n.t('fillRequiredFields', { fields: missing.map(f => f.label.replace(/\s*\*$/, '')).join(', ') }), 'warning')
            return
        }

        $.ajax({
            url:         apiUrl,
            type:        'POST',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify(buildBody(values)),
            success: function (item) {
                overlay.remove()
                onSuccess(item)
                showToast(I18n.t('createdSuccess'), 'success')
            },
            error: function (xhr) {
                showToast(xhr.responseJSON?.message ?? I18n.t('errorCreating'), 'error')
            }
        })
    })
}
