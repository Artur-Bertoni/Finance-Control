import { I18n } from '../i18n.js'

/**
 * Opens a confirmation dialog with cancel/confirm buttons.
 * @param {string} message
 * @param {() => void} onConfirm
 * @param {string|null} title
 */
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
    overlay.addEventListener('click', e => { if (e.target === overlay) overlay.remove() })
}

/**
 * Opens a confirmation dialog and returns a Promise<boolean>.
 * Resolves true if confirmed, false if cancelled.
 * @param {string} message
 * @param {string|null} title
 * @param {{ cancelLabel?: string, confirmLabel?: string, confirmClass?: string }} opts
 * @returns {Promise<boolean>}
 */
export function showConfirmAsync(message, title = null, opts = {}) {
    const cancelLabel  = opts.cancelLabel  ?? I18n.t('stay')
    const confirmLabel = opts.confirmLabel ?? I18n.t('leaveAnyway')
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
