import { I18n } from '../i18n.js'

export function createIconPickerField() {
    const box = document.createElement('div')

    const trigger = document.createElement('button')
    trigger.type      = 'button'
    trigger.className = 'icon-picker-trigger'
    trigger.id        = 'icon-picker-trigger'
    trigger.setAttribute('aria-haspopup', 'true')
    trigger.setAttribute('aria-expanded', 'false')
    trigger.setAttribute('aria-controls', 'icon-picker-dropdown')

    const preview = document.createElement('span')
    preview.className = 'icon-picker-preview'
    preview.id        = 'icon-picker-preview'
    preview.innerHTML = '<i class="ph ph-tag" style="font-size:20px"></i>'
    trigger.appendChild(preview)

    const label = document.createElement('span')
    label.className   = 'icon-picker-label'
    label.id          = 'icon-picker-label'
    label.textContent = I18n.t('iconPickerChoose')
    trigger.appendChild(label)

    const hidden = document.createElement('input')
    hidden.type = 'hidden'
    hidden.id   = 'icon-key-input'
    trigger.appendChild(hidden)

    const dropdown = document.createElement('dialog')
    dropdown.className = 'icon-picker-dropdown'
    dropdown.id        = 'icon-picker-dropdown'

    const search = document.createElement('input')
    search.type        = 'text'
    search.className   = 'icon-picker-search'
    search.id          = 'icon-picker-search'
    search.placeholder = I18n.t('searchPlaceholder')
    search.autocomplete = 'off'
    dropdown.appendChild(search)

    const grid = document.createElement('div')
    grid.className = 'icon-picker-grid'
    grid.id        = 'icon-picker-grid'
    dropdown.appendChild(grid)

    box.append(trigger, dropdown)
    return box
}
