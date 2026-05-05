export function navigate(url) {
    window.location.href = url
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

export function showToast(message, type = 'info') {
    // Importar inline para evitar dependência circular
    import('./IconLibrary.js').then(({ Icons }) => {
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
            <button class="toast-close" aria-label="Fechar">×</button>
        `

        toast.querySelector('.toast-close').addEventListener('click', () => dismissToast(toast))
        container.appendChild(toast)

        setTimeout(() => dismissToast(toast), 4500)
    })
}

function dismissToast(toast) {
    if (toast.classList.contains('toast-closing')) return
    toast.classList.add('toast-closing')
    setTimeout(() => toast.remove(), 260)
}

export function showConfirm(message, onConfirm, title = 'Confirmar ação') {
    const overlay = document.createElement('div')
    overlay.className = 'modal-overlay'
    overlay.innerHTML = `
        <div class="modal-card">
            <p class="modal-title">${title}</p>
            <p class="modal-message">${message}</p>
            <div class="modal-actions">
                <button class="btn btn-secondary" id="modal-cancel-btn">Cancelar</button>
                <button class="btn btn-danger"    id="modal-confirm-btn">Confirmar</button>
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

const TRASH_SVG = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8.75 1A2.75 2.75 0 006 3.75v.443c-.795.077-1.584.176-2.365.298a.75.75 0 10.23 1.482l.149-.022.841 10.518A2.75 2.75 0 007.596 19h4.807a2.75 2.75 0 002.742-2.53l.841-10.52.149.023a.75.75 0 00.23-1.482A41.03 41.03 0 0014 4.193V3.75A2.75 2.75 0 0011.25 1h-2.5zM10 4c.84 0 1.673.025 2.5.075V3.75c0-.69-.56-1.25-1.25-1.25h-2.5c-.69 0-1.25.56-1.25 1.25v.325C8.327 4.025 9.16 4 10 4zM8.58 7.72a.75.75 0 00-1.5.06l.3 7.5a.75.75 0 101.5-.06l-.3-7.5zm4.34.06a.75.75 0 10-1.5-.06l-.3 7.5a.75.75 0 101.5.06l.3-7.5z" clip-rule="evenodd"/></svg>`

/**
 * Adiciona um botão de deletar na área de ações do header
 * @returns {HTMLElement} O botão criado
 */
export function addDeleteIcon() {
    import('./IconLibrary.js').then(({ Icons }) => {
        const btn = document.createElement('button')
        btn.className = 'btn btn-danger btn-sm'
        btn.id = 'delete-btn'
        btn.type = 'button'
        btn.innerHTML = `${Icons.delete()} Excluir`

        const container = document.getElementById('header-actions')
        if (container) container.appendChild(btn)
    })

    return { addEventListener: () => {} }
}

export function addHomePageIcon() {
    return { addEventListener: () => {} }
}

export function showQuickAdd({ title, fields, apiUrl, buildBody, onSuccess }) {
    const overlay = document.createElement('div')
    overlay.className = 'modal-overlay'

    const PLUS_SVG = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clip-rule="evenodd"/></svg>'

    const renderInput = (f) => {
        if (f.type === 'textarea')
            return `<textarea id="qaf-${f.id}" placeholder="${f.placeholder ?? ''}"></textarea>`
        if (f.type === 'select') {
            const opts = (f.options ?? []).map(o => `<option value="${o.value}">${o.label}</option>`).join('')
            const sel  = `<select id="qaf-${f.id}"><option value="">— ${f.placeholder ?? 'Selecione'} —</option>${opts}</select>`
            if (!f.addBtn) return sel
            return (
                '<div class="select-with-add">' + sel +
                '<button type="button" class="btn-add-inline" id="qaf-' + f.id + '-add-btn" title="' + (f.addBtn.title ?? 'Novo') + '">' + PLUS_SVG + '</button>' +
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
                <button class="btn btn-secondary" id="qa-cancel">Cancelar</button>
                <button class="btn btn-primary"   id="qa-save">Salvar</button>
            </div>
        </div>
    `
    document.body.appendChild(overlay)

    // Wire up any nested add-buttons for select fields
    fields.filter(f => f.type === 'select' && f.addBtn).forEach(f => {
        const addBtn = overlay.querySelector('#qaf-' + f.id + '-add-btn')
        if (!addBtn) return
        addBtn.addEventListener('click', () => {
            showQuickAdd({
                title:     f.addBtn.title  ?? 'Novo',
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
            showToast(`Preencha: ${missing.map(f => f.label.replace(/\s*\*$/, '')).join(', ')}`, 'warning')
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
                showToast('Criado com sucesso!', 'success')
            },
            error: function (xhr) {
                showToast(xhr.responseJSON?.message ?? 'Erro ao criar.', 'error')
            }
        })
    })
}
