import { Icons } from '../icons/IconLibrary.js'
import { I18n } from '../i18n.js'
import { showConfirmAsync, showToast } from '../../utils/FrontendFunctions.js'
import { filterIconGroups, renderIconGroups } from '../components/IconPicker.js'
import { createModal } from '../components/Modal.js'

export function showQuickAdd({ title, fields, apiUrl, buildBody, onSuccess, successToast = true }) {
    const renderInput = (f) => {
        if (f.type === 'icon-picker') {
            return (
                '<div class="qa-icon-field" id="qaf-' + f.id + '-wrapper">' +
                  '<button type="button" class="qa-icon-trigger" id="qaf-' + f.id + '-trigger">' +
                    '<span class="qa-icon-preview" id="qaf-' + f.id + '-preview"><i class="ph ph-tag"></i></span>' +
                    '<span class="qa-icon-label" id="qaf-' + f.id + '-label">' + I18n.t('iconPickerChoose') + '</span>' +
                  '</button>' +
                  '<input type="hidden" id="qaf-' + f.id + '">' +
                  '<div class="qa-icon-dropdown" id="qaf-' + f.id + '-dropdown">' +
                    '<input type="text" class="qa-icon-search" id="qaf-' + f.id + '-search" placeholder="' + I18n.t('searchPlaceholder') + '">' +
                    '<div class="qa-icon-grid" id="qaf-' + f.id + '-grid"></div>' +
                  '</div>' +
                '</div>'
            )
        }
        if (f.type === 'textarea')
            return `<textarea id="qaf-${f.id}" placeholder="${f.placeholder ?? ''}"></textarea>`
        if (f.type === 'select') {
            const opts = (f.options ?? []).map(o => `<option value="${o.value}">${o.label}</option>`).join('')
            const sel  = `<select id="qaf-${f.id}"><option value=""> ${f.placeholder ?? I18n.t('selectAccount')} </option>${opts}</select>`
            if (!f.addBtn) return sel
            return (
                '<div class="select-with-add">' + sel +
                '<button type="button" class="btn-add-inline" id="qaf-' + f.id + '-add-btn" title="' + (f.addBtn.btnTitle ?? f.addBtn.title ?? '') + '">' + Icons.add() + '</button>' +
                '</div>'
            )
        }
        const stepAttr  = f.step ? ' step="' + f.step + '"' : ''
        const valueAttr = f.value ? ' value="' + f.value + '"' : ''
        return '<input id="qaf-' + f.id + '" type="' + (f.type ?? 'text') + '" placeholder="' + (f.placeholder ?? '') + '"' + stepAttr + valueAttr + '>'
    }

    const fieldsHtml = fields.map(f => {
        const inputHtml = renderInput(f)
        const labelText = f.label.replace(/\s*\*$/, '')
        const marker = f.required
            ? '<span class="required-mark"> *</span>'
            : '<span class="optional-label"> ' + I18n.t('commonOptional') + '</span>'
        return '<div class="field"><label>' + labelText + marker + '</label>' + inputHtml + '</div>'
    }).join('')

    const body = document.createElement('div')
    body.className = 'quick-add-fields'
    body.innerHTML = fieldsHtml

    const modal = createModal({
        title,
        cardClass: 'quick-add-card',
        body,
        actions: [
            { id: 'qa-cancel', label: I18n.t('commonCancel') },
            { id: 'qa-save',   label: I18n.t('commonSave'), variant: 'primary', closes: false },
        ],
    })
    const overlay = modal.overlay

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

    fields.filter(f => f.type === 'icon-picker').forEach(f => {
        const wrapper  = overlay.querySelector(`#qaf-${f.id}-wrapper`)
        const trigger  = overlay.querySelector(`#qaf-${f.id}-trigger`)
        const dropdown = overlay.querySelector(`#qaf-${f.id}-dropdown`)
        const search   = overlay.querySelector(`#qaf-${f.id}-search`)
        const grid     = overlay.querySelector(`#qaf-${f.id}-grid`)
        const hidden   = overlay.querySelector(`#qaf-${f.id}`)
        const preview  = overlay.querySelector(`#qaf-${f.id}-preview`)
        const label    = overlay.querySelector(`#qaf-${f.id}-label`)
        if (!trigger || !dropdown || !grid) return

        function renderGrid(groups) {
            renderIconGroups(grid, groups, {
                groupClass: 'qa-icon-group',
                itemClass: 'qa-icon-item',
                emptyClass: 'qa-icon-empty',
                onSelect: (icon, iconLabelText) => {
                    hidden.value = icon.key
                    preview.innerHTML = `<i class="ph ${icon.key}"></i>`
                    label.textContent = iconLabelText
                    dropdown.style.display = 'none'
                }
            })
        }

        renderGrid(filterIconGroups(''))

        trigger.addEventListener('click', e => {
            e.stopPropagation()
            const isOpen = dropdown.style.display === 'flex'
            dropdown.style.display = isOpen ? 'none' : 'flex'
            if (!isOpen) search?.focus()
        })

        search?.addEventListener('input', () => {
            renderGrid(filterIconGroups(search.value))
        })

        const closeOnOutside = e => {
            if (!wrapper.contains(e.target)) dropdown.style.display = 'none'
        }
        document.addEventListener('click', closeOnOutside)
        overlay.querySelector('#qa-cancel').addEventListener('click', () => document.removeEventListener('click', closeOnOutside), { once: true })
    })

    fields.filter(f => f.required).forEach(f => {
        const el = overlay.querySelector('#qaf-' + f.id)
        if (el) el.addEventListener('input',  () => el.classList.remove('field-error'))
        if (el) el.addEventListener('change', () => el.classList.remove('field-error'))
    })

    overlay.querySelector('#qa-save').addEventListener('click', async () => {
        const values = {}
        fields.forEach(f => { values[f.id] = overlay.querySelector('#qaf-' + f.id)?.value ?? '' })

        fields.forEach(f => overlay.querySelector('#qaf-' + f.id)?.classList.remove('field-error'))

        const missing = fields.filter(f => f.required && !values[f.id])
        if (missing.length) {
            missing.forEach(f => overlay.querySelector('#qaf-' + f.id)?.classList.add('field-error'))
            showToast(I18n.t('commonFillRequired', { fields: missing.map(f => f.label.replace(/\s*\*$/, '')).join(', ') }), 'warning')
            return
        }

        let result = null
        $.ajax({
            url: apiUrl, type: 'POST', async: false, contentType: 'application/json',
            data: JSON.stringify(buildBody(values)),
            success: item => { result = { ok: true, item } },
            error:   xhr  => { result = { ok: false, xhr } }
        })

        if (!result.ok) {
            if (result.xhr.responseJSON?.errorCode === 'error.duplicate.name') {
                const proceed = await showConfirmAsync(
                    I18n.t('duplicateItemConfirm', { name: values.name }),
                    null,
                    { cancelLabel: I18n.t('commonCancel'), confirmLabel: I18n.t('createAnyway'), confirmClass: 'btn-primary' }
                )
                if (!proceed) return
                $.ajax({
                    url: apiUrl + '?force=true', type: 'POST', async: false, contentType: 'application/json',
                    data: JSON.stringify(buildBody(values)),
                    success: item => { result = { ok: true, item } },
                    error:   xhr  => { result = { ok: false, xhr } }
                })
            }
            if (!result.ok) {
                showToast(result.xhr.responseJSON?.message ?? I18n.t('errorCreating'), 'error')
                return
            }
        }

        modal.close()
        onSuccess(result.item)
        if (successToast) showToast(I18n.t('createdSuccess'), 'success')
    })
}
