import { createActionButtons, setActionButton } from './ActionButtons.js'

export function createActionBar({ id, message = '', barClass = null, actions = [] } = {}) {
    if (id) document.getElementById(id)?.remove()

    const bar = document.createElement('div')
    bar.className = 'confirm-bar'
    if (barClass) bar.classList.add(...barClass.split(' ').filter(Boolean))
    if (id) bar.id = id

    const messageEl = document.createElement('p')
    messageEl.className = 'confirm-bar-message'
    messageEl.textContent = message
    bar.appendChild(messageEl)

    const close = () => bar.remove()
    bar.appendChild(createActionButtons(actions.map(a => ({ size: 'sm', ...a })), 'confirm-bar-actions', close))

    document.body.appendChild(bar)

    return {
        bar,
        close,
        setMessage: text => { messageEl.textContent = text },
        setAction:  (actionId, state) => setActionButton(bar, actionId, state),
    }
}
