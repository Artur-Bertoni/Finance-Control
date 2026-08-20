import { I18n } from '../i18n.js'

export function showConfirm(message, onConfirm, title = null, opts = {}) {
    const extra = opts.extraAction

    const overlay = document.createElement('div')
    overlay.className = 'modal-overlay'
    overlay.innerHTML = `
        <div class="modal-card">
            <p class="modal-title">${title ?? I18n.t('confirmAction')}</p>
            <p class="modal-message">${message}</p>
            <div class="modal-actions">
                ${extra ? `<button class="btn btn-link modal-actions-start" id="modal-extra-btn">${extra.label}</button>` : ''}
                <button class="btn btn-secondary" id="modal-cancel-btn">${I18n.t('commonCancel')}</button>
                <button class="btn btn-danger"    id="modal-confirm-btn">${opts.confirmLabel ?? I18n.t('commonConfirm')}</button>
            </div>
        </div>
    `
    document.body.appendChild(overlay)

    overlay.querySelector('#modal-cancel-btn').addEventListener('click', () => overlay.remove())
    overlay.querySelector('#modal-confirm-btn').addEventListener('click', () => {
        overlay.remove()
        onConfirm()
    })
    overlay.querySelector('#modal-extra-btn')?.addEventListener('click', () => {
        overlay.remove()
        extra.onClick()
    })
    overlay.addEventListener('click', e => { if (e.target === overlay) overlay.remove() })
}

export function showConfirmBar(message, onConfirm, opts = {}) {
    document.getElementById('confirm-bar')?.remove()

    const bar = document.createElement('div')
    bar.className = 'confirm-bar'
    bar.id = 'confirm-bar'
    bar.innerHTML = `
        <p class="confirm-bar-message"></p>
        <div class="confirm-bar-actions">
            <button class="btn btn-secondary btn-sm" id="confirm-bar-cancel">${opts.cancelLabel ?? I18n.t('commonCancel')}</button>
            <button class="btn btn-danger btn-sm"    id="confirm-bar-confirm">${opts.confirmLabel ?? I18n.t('commonConfirm')}</button>
        </div>
    `
    bar.querySelector('.confirm-bar-message').textContent = message
    document.body.appendChild(bar)

    const close = () => bar.remove()
    bar.querySelector('#confirm-bar-cancel').addEventListener('click', () => {
        close()
        opts.onCancel?.()
    })
    bar.querySelector('#confirm-bar-confirm').addEventListener('click', () => {
        close()
        onConfirm()
    })
}

export function showConfirmAsync(message, title = null, opts = {}) {
    const cancelLabel  = opts.cancelLabel  ?? I18n.t('commonStay')
    const confirmLabel = opts.confirmLabel ?? I18n.t('commonLeaveAnyway')
    const confirmClass = opts.confirmClass ?? 'btn-danger'
    return new Promise(resolve => {
        const overlay = document.createElement('div')
        overlay.className = 'modal-overlay'
        overlay.innerHTML = `
            <div class="modal-card">
                <p class="modal-title">${title ?? I18n.t('confirmAction')}</p>
                <p class="modal-message">${message}</p>
                <div class="modal-actions">
                    <button class="btn btn-secondary" id="modal-cancel-btn">${cancelLabel}</button>
                    <button class="btn ${confirmClass}" id="modal-confirm-btn">${confirmLabel}</button>
                </div>
            </div>
        `
        document.body.appendChild(overlay)
        const cancel  = () => { overlay.remove(); resolve(false) }
        const confirm = () => { overlay.remove(); resolve(true) }
        overlay.querySelector('#modal-cancel-btn').addEventListener('click', cancel)
        overlay.querySelector('#modal-confirm-btn').addEventListener('click', confirm)
        overlay.addEventListener('click', e => { if (e.target === overlay) cancel() })
    })
}
