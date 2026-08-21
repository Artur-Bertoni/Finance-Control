import { createActionButtons } from './ActionButtons.js'

export function createModal({
    title = null,
    message = null,
    messageHtml = null,
    body = null,
    id = null,
    cardClass = null,
    actions = [],
    dismissible = true,
    onDismiss = null,
} = {}) {
    const overlay = document.createElement('div')
    overlay.className = 'modal-overlay'
    if (id) overlay.id = id

    const card = document.createElement('div')
    card.className = 'modal-card'
    if (cardClass) card.classList.add(...cardClass.split(' ').filter(Boolean))
    overlay.appendChild(card)

    if (title !== null) {
        const titleEl = document.createElement('p')
        titleEl.className = 'modal-title'
        titleEl.textContent = title
        card.appendChild(titleEl)
    }

    if (message !== null || messageHtml !== null) {
        const messageEl = document.createElement('p')
        messageEl.className = 'modal-message'
        if (messageHtml !== null) messageEl.innerHTML = messageHtml
        else                      messageEl.textContent = message
        card.appendChild(messageEl)
    }

    if (body) card.appendChild(body)

    const close = () => overlay.remove()

    if (actions.length) {
        card.appendChild(createActionButtons(actions, 'modal-actions', close))
    }

    if (dismissible) {
        let pressedOnOverlay = false
        overlay.addEventListener('mousedown', e => { pressedOnOverlay = e.target === overlay })
        overlay.addEventListener('click', e => {
            if (e.target !== overlay || !pressedOnOverlay) return
            close()
            onDismiss?.()
        })
    }

    document.body.appendChild(overlay)
    return { overlay, card, close }
}
