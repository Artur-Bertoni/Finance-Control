const VARIANT_CLASS = {
    primary:   'btn-primary',
    secondary: 'btn-secondary',
    danger:    'btn-danger',
    ghost:     'btn-ghost',
    link:      'btn-link',
}

export function createActionButtons(actions, containerClass, close) {
    const row = document.createElement('div')
    row.className = containerClass

    for (const action of actions) {
        if (!action) continue
        row.appendChild(createActionButton(action, close))
    }
    return row
}

export function createActionButton({ id, label, variant = 'secondary', size = null, align = null, disabled = false, closes = true, onClick }, close) {
    const btn = document.createElement('button')
    btn.type = 'button'
    btn.className = `btn ${VARIANT_CLASS[variant] ?? VARIANT_CLASS.secondary}`
    if (size === 'sm') btn.classList.add('btn-sm')
    if (align === 'start') btn.classList.add('modal-actions-start')
    if (id) btn.id = id
    btn.textContent = label ?? ''
    btn.disabled = disabled

    btn.addEventListener('click', () => {
        if (closes) close?.()
        onClick?.()
    })
    return btn
}

export function setActionButton(container, id, { label, disabled } = {}) {
    const btn = container?.querySelector(`#${id}`)
    if (!btn) return
    if (label !== undefined)    btn.textContent = label
    if (disabled !== undefined) btn.disabled = disabled
}
