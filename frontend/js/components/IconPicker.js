import { I18n } from '../i18n.js'

// Curated Phosphor icon set for financial categories
export const CATEGORY_ICONS = [
    { key: 'ph-fork-knife',          label: 'Alimentação' },
    { key: 'ph-shopping-cart',       label: 'Compras' },
    { key: 'ph-car',                 label: 'Transporte' },
    { key: 'ph-gas-pump',            label: 'Combustível' },
    { key: 'ph-house',               label: 'Moradia' },
    { key: 'ph-heartbeat',           label: 'Saúde' },
    { key: 'ph-pill',                label: 'Medicamentos' },
    { key: 'ph-graduation-cap',      label: 'Educação' },
    { key: 'ph-books',               label: 'Livros' },
    { key: 'ph-game-controller',     label: 'Lazer' },
    { key: 'ph-music-notes',         label: 'Entretenimento' },
    { key: 'ph-film-strip',          label: 'Cinema' },
    { key: 'ph-airplane',            label: 'Viagem' },
    { key: 'ph-suitcase',            label: 'Viagem' },
    { key: 'ph-t-shirt',             label: 'Roupas' },
    { key: 'ph-sneaker',             label: 'Calçados' },
    { key: 'ph-lightning',           label: 'Energia' },
    { key: 'ph-drop',               label: 'Água' },
    { key: 'ph-wifi-high',          label: 'Internet' },
    { key: 'ph-device-mobile',      label: 'Celular' },
    { key: 'ph-monitor',            label: 'Tecnologia' },
    { key: 'ph-paw-print',          label: 'Pet' },
    { key: 'ph-baby',               label: 'Filhos' },
    { key: 'ph-gift',               label: 'Presentes' },
    { key: 'ph-hand-coins',         label: 'Salário' },
    { key: 'ph-currency-dollar',    label: 'Renda' },
    { key: 'ph-trend-up',           label: 'Investimento' },
    { key: 'ph-piggy-bank',         label: 'Poupança' },
    { key: 'ph-bank',               label: 'Banco' },
    { key: 'ph-credit-card',        label: 'Cartão' },
    { key: 'ph-receipt',            label: 'Contas' },
    { key: 'ph-buildings',          label: 'Empresa' },
    { key: 'ph-briefcase',          label: 'Trabalho' },
    { key: 'ph-hammer',             label: 'Reforma' },
    { key: 'ph-paint-brush',        label: 'Arte' },
    { key: 'ph-bicycle',            label: 'Bicicleta' },
    { key: 'ph-bus',                label: 'Ônibus' },
    { key: 'ph-train',              label: 'Metrô' },
    { key: 'ph-coffee',             label: 'Café' },
    { key: 'ph-beer-stein',         label: 'Bar' },
    { key: 'ph-pizza',              label: 'Delivery' },
    { key: 'ph-cookie',             label: 'Doces' },
    { key: 'ph-barbell',            label: 'Academia' },
    { key: 'ph-first-aid-kit',      label: 'Emergência' },
    { key: 'ph-sun',                label: 'Férias' },
    { key: 'ph-star',               label: 'Especial' },
    { key: 'ph-tag',                label: 'Geral' },
    { key: 'ph-question',           label: 'Outros' },
]

export class IconPicker {
    static _current = null

    static init(onSelect) {
        const trigger    = document.getElementById('icon-picker-trigger')
        const dialog     = document.getElementById('icon-picker-dropdown')
        const searchInput = document.getElementById('icon-picker-search')
        if (!trigger || !dialog) return

        IconPicker._renderGrid(CATEGORY_ICONS, onSelect)

        trigger.addEventListener('click', (e) => {
            e.stopPropagation()
            if (dialog.open) {
                dialog.close()
                trigger.setAttribute('aria-expanded', 'false')
            } else {
                IconPicker._positionDialog(trigger, dialog)
                dialog.show()
                trigger.setAttribute('aria-expanded', 'true')
                searchInput?.focus()
            }
        })

        searchInput?.addEventListener('input', (e) => {
            const q = e.target.value.trim().toLowerCase()
            const filtered = q
                ? CATEGORY_ICONS.filter(ic => ic.key.replace('ph-', '').includes(q) || ic.label.toLowerCase().includes(q))
                : CATEGORY_ICONS
            IconPicker._renderGrid(filtered, onSelect)
        })

        document.addEventListener('click', (e) => {
            if (dialog.open && !dialog.contains(e.target) && e.target !== trigger) {
                dialog.close()
                trigger.setAttribute('aria-expanded', 'false')
            }
        })

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && dialog.open) {
                dialog.close()
                trigger.setAttribute('aria-expanded', 'false')
                trigger.focus()
            }
        })

        I18n.onChange(() => {
            const searchInput2 = document.getElementById('icon-picker-search')
            if (searchInput2) searchInput2.placeholder = I18n.t('searchPlaceholder')
        })
    }

    static _positionDialog(trigger, dialog) {
        const rect = trigger.getBoundingClientRect()
        dialog.style.top  = `${rect.bottom + 4}px`
        dialog.style.left = `${rect.left}px`
        dialog.style.width = `${Math.max(rect.width, 300)}px`
    }

    static setValue(iconKey) {
        const preview = document.getElementById('icon-picker-preview')
        const label   = document.getElementById('icon-picker-label')
        const hidden  = document.getElementById('icon-key-input')
        if (!preview || !label || !hidden) return
        hidden.value = iconKey ?? ''
        if (iconKey) {
            preview.innerHTML = `<i class="ph ${iconKey}" style="font-size:20px"></i>`
            const entry = CATEGORY_ICONS.find(ic => ic.key === iconKey)
            label.textContent = entry?.label ?? iconKey.replace('ph-', '')
        } else {
            preview.innerHTML = `<i class="ph ph-tag" style="font-size:20px"></i>`
            label.textContent = I18n.t('iconPickerChoose')
            label.dataset.i18n = 'iconPickerChoose'
        }
    }

    static getValue() {
        return document.getElementById('icon-key-input')?.value || null
    }

    static _renderGrid(icons, onSelect) {
        const grid = document.getElementById('icon-picker-grid')
        if (!grid) return
        grid.innerHTML = ''
        if (!icons.length) {
            grid.innerHTML = `<span class="icon-picker-empty">${I18n.t('noSearchResults', { query: '' }).replace(' ""', '')}</span>`
            return
        }
        for (const ic of icons) {
            const btn = document.createElement('button')
            btn.type = 'button'
            btn.className = 'icon-picker-item'
            btn.title = ic.label
            btn.setAttribute('aria-label', ic.label)
            btn.innerHTML = `<i class="ph ${ic.key}"></i>`
            btn.addEventListener('click', () => {
                onSelect(ic.key)
                const dialog = document.getElementById('icon-picker-dropdown')
                const trigger = document.getElementById('icon-picker-trigger')
                dialog?.close()
                trigger?.setAttribute('aria-expanded', 'false')
                trigger?.focus()
            })
            grid.appendChild(btn)
        }
    }
}
