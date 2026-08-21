import { createModal } from '../components/Modal.js'
import { createActionBar } from '../components/ActionBar.js'
import { I18n } from '../i18n.js'

export function showConfirm(message, onConfirm, title = null, opts = {}) {
    const extra = opts.extraAction

    createModal({
        title: title ?? I18n.t('confirmAction'),
        messageHtml: message,
        actions: [
            extra && { id: 'modal-extra-btn', label: extra.label, variant: 'link', align: 'start', onClick: extra.onClick },
            { id: 'modal-cancel-btn',  label: I18n.t('commonCancel') },
            { id: 'modal-confirm-btn', label: opts.confirmLabel ?? I18n.t('commonConfirm'), variant: 'danger', onClick: onConfirm },
        ],
    })
}

export function showConfirmBar(message, onConfirm, opts = {}) {
    return createActionBar({
        id: 'confirm-bar',
        message,
        actions: [
            { id: 'confirm-bar-cancel',  label: opts.cancelLabel ?? I18n.t('commonCancel'), onClick: opts.onCancel },
            { id: 'confirm-bar-confirm', label: opts.confirmLabel ?? I18n.t('commonConfirm'), variant: 'danger', onClick: onConfirm },
        ],
    })
}

export function showConfirmAsync(message, title = null, opts = {}) {
    return new Promise(resolve => {
        createModal({
            title: title ?? I18n.t('confirmAction'),
            messageHtml: message,
            onDismiss: () => resolve(false),
            actions: [
                { id: 'modal-cancel-btn',  label: opts.cancelLabel ?? I18n.t('commonStay'), onClick: () => resolve(false) },
                { id: 'modal-confirm-btn', label: opts.confirmLabel ?? I18n.t('commonLeaveAnyway'), variant: variantOf(opts.confirmClass), onClick: () => resolve(true) },
            ],
        })
    })
}

function variantOf(confirmClass) {
    return (confirmClass ?? 'btn-danger').replace('btn-', '')
}
