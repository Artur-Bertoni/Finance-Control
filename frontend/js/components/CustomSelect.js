import { I18n } from '../i18n.js'

export class CustomSelect {
    static _instances = []

    constructor(select) {
        this.select       = select
        this.wrapper      = null
        this.trigger      = null
        this.dropdown     = null
        this._searchInput = null
        this._build()
        this._patchValueSetter()
        this._patchOptions()
        this._observe()
        CustomSelect._instances.push(this)
    }

    static wrap(select) {
        return new CustomSelect(select)
    }

    static autoInit() {
        CustomSelect._initNew()
        new MutationObserver(() => {
            if (document.querySelector('select:not([data-cs-init])')) CustomSelect._initNew()
        }).observe(document.body, { childList: true, subtree: true })
    }

    static _initNew() {
        document.querySelectorAll('select:not([data-cs-init])').forEach(sel => new CustomSelect(sel))
    }

    static syncAll() {
        CustomSelect._instances = CustomSelect._instances.filter(cs => document.contains(cs.wrapper))
        CustomSelect._instances.forEach(cs => cs._syncDisplay())
    }

    _build() {
        const sel = this.select
        sel.dataset.csInit = '1'
        sel.hidden = true

        const wrapper = document.createElement('div')
        wrapper.className = 'cs-wrapper'
        sel.parentNode.insertBefore(wrapper, sel)
        wrapper.appendChild(sel)

        const trigger = document.createElement('div')
        trigger.className = 'cs-trigger'
        trigger.tabIndex = 0
        trigger.setAttribute('role', 'combobox')
        trigger.setAttribute('aria-expanded', 'false')
        wrapper.appendChild(trigger)

        const dropdown = document.createElement('div')
        dropdown.className = 'cs-dropdown'
        wrapper.appendChild(dropdown)

        this.wrapper  = wrapper
        this.trigger  = trigger
        this.dropdown = dropdown

        this._syncDisplay()

        trigger.addEventListener('click', () => this._toggle())
        trigger.addEventListener('keydown', e => {
            if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); this._toggle() }
            if (e.key === 'Escape')    { this._close(); trigger.focus() }
            if (e.key === 'ArrowDown') { e.preventDefault(); this._open() }
            if (e.key === 'ArrowUp')   { e.preventDefault(); this._open(); requestAnimationFrame(() => this._getVisibleOptions().at(-1)?.focus()) }
            if (e.key === 'Tab')       this._close()
        })

        document.addEventListener('click', e => {
            if (!wrapper.contains(e.target)) this._close()
        }, true)
    }

    _syncDisplay() {
        const sel     = this.select
        const opt     = sel.options[sel.selectedIndex]
        const isEmpty = !opt || opt.disabled || opt.value === ''
        this.trigger.replaceChildren()
        if (!isEmpty && opt.dataset.iconKey) {
            const icon = document.createElement('i')
            icon.className = `ph ${opt.dataset.iconKey}`
            this.trigger.appendChild(icon)
        }
        const label = document.createElement('span')
        label.textContent = opt ? opt.text : ''
        this.trigger.appendChild(label)
        this.trigger.classList.toggle('cs-placeholder', isEmpty)
        this.trigger.classList.toggle('field-error', sel.classList.contains('field-error'))
    }

    _buildOptions() {
        const sel = this.select
        this.dropdown.innerHTML = ''
        this._searchInput = null

        // --- Search input ---
        const searchWrapper = document.createElement('div')
        searchWrapper.className = 'cs-search-wrapper'
        const searchInput = document.createElement('input')
        searchInput.type = 'text'
        searchInput.className = 'cs-search'
        searchInput.placeholder = I18n.t('filterOptions')
        searchInput.autocomplete = 'off'
        searchWrapper.appendChild(searchInput)
        this.dropdown.appendChild(searchWrapper)
        this._searchInput = searchInput

        // --- Options container (scrollable) ---
        const optsList = document.createElement('div')
        optsList.className = 'cs-options'
        this.dropdown.appendChild(optsList)

        // --- Clear option ---
        if (sel.value !== '') {
            const clearItem = document.createElement('div')
            clearItem.className = 'cs-option cs-clear'
            clearItem.textContent = I18n.t('clearSelection')
            clearItem.tabIndex = -1
            const doClear = () => {
                sel.selectedIndex = 0
                sel.dispatchEvent(new Event('change', { bubbles: true }))
                this._close()
                this.trigger.focus()
            }
            clearItem.addEventListener('mousedown', e => { e.preventDefault(); doClear() })
            clearItem.addEventListener('keydown', e => {
                if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); doClear() }
                if (e.key === 'Escape')    { this._close(); this.trigger.focus() }
                if (e.key === 'ArrowDown') {
                    e.preventDefault()
                    const visible = this._getVisibleOptions()
                    const idx = visible.indexOf(clearItem)
                    visible[idx + 1]?.focus()
                }
                if (e.key === 'ArrowUp')   { e.preventDefault(); this._searchInput?.focus() }
                if (e.key === 'Tab')       this._close()
            })
            optsList.appendChild(clearItem)
        }

        // --- Regular options ---
        Array.from(sel.options).forEach(opt => {
            if (opt.disabled) return
            const item = document.createElement('div')
            item.className = 'cs-option' + (opt.selected ? ' cs-selected' : '')
            item.tabIndex = -1
            item.dataset.value = opt.value
            if (opt.dataset.iconKey) {
                const icon = document.createElement('i')
                icon.className = `ph ${opt.dataset.iconKey}`
                item.appendChild(icon)
            }
            const label = document.createElement('span')
            label.textContent = opt.text
            item.appendChild(label)
            item.addEventListener('mousedown', e => {
                e.preventDefault()
                sel.value = opt.value
                sel.dispatchEvent(new Event('change', { bubbles: true }))
                this._close()
                this.trigger.focus()
            })
            item.addEventListener('keydown', e => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault()
                    sel.value = opt.value
                    sel.dispatchEvent(new Event('change', { bubbles: true }))
                    this._close()
                    this.trigger.focus()
                }
                if (e.key === 'Escape')    { this._close(); this.trigger.focus() }
                if (e.key === 'ArrowDown') {
                    e.preventDefault()
                    const visible = this._getVisibleOptions()
                    const idx = visible.indexOf(item)
                    visible[idx + 1]?.focus()
                }
                if (e.key === 'ArrowUp') {
                    e.preventDefault()
                    const visible = this._getVisibleOptions()
                    const idx = visible.indexOf(item)
                    if (idx === 0) this._searchInput?.focus()
                    else visible[idx - 1]?.focus()
                }
                if (e.key === 'Tab') this._close()
            })
            optsList.appendChild(item)
        })

        // --- No results placeholder ---
        const noResults = document.createElement('div')
        noResults.className = 'cs-no-results'
        noResults.textContent = I18n.t('noResults')
        noResults.style.display = 'none'
        optsList.appendChild(noResults)

        // --- Filter logic ---
        searchInput.addEventListener('input', () => this._applyFilter())
        searchInput.addEventListener('keydown', e => {
            if (e.key === 'ArrowDown') {
                e.preventDefault()
                this._getVisibleOptions()[0]?.focus()
            }
            if (e.key === 'Escape') { this._close(); this.trigger.focus() }
            if (e.key === 'Tab')    this._close()
            if (e.key === 'Enter') {
                e.preventDefault()
                const visible = this._getVisibleOptions().filter(i => !i.classList.contains('cs-clear'))
                if (visible.length === 1) {
                    sel.value = visible[0].dataset.value
                    sel.dispatchEvent(new Event('change', { bubbles: true }))
                    this._close()
                    this.trigger.focus()
                }
            }
        })
    }

    _applyFilter() {
        const norm = s => s.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '')
        const query = norm(this._searchInput?.value || '')
        const opts = [...this.dropdown.querySelectorAll('.cs-options .cs-option:not(.cs-clear)')]
        let hasVisible = false
        opts.forEach(item => {
            const label = item.querySelector('span') ?? item
            const text = norm(label.textContent)
            const visible = text.includes(query)
            item.style.display = visible ? '' : 'none'
            if (visible) hasVisible = true
        })
        const noResults = this.dropdown.querySelector('.cs-no-results')
        if (noResults) noResults.style.display = hasVisible ? 'none' : ''
    }

    _getVisibleOptions() {
        return [...this.dropdown.querySelectorAll('.cs-options .cs-option')].filter(i => i.style.display !== 'none')
    }

    _toggle() {
        this.wrapper.classList.contains('cs-open') ? this._close() : this._open()
    }

    _open() {
        if (this.wrapper.classList.contains('cs-open')) return
        this._buildOptions()
        document.querySelectorAll('.cs-wrapper.cs-open').forEach(w => w.classList.remove('cs-open'))
        this.wrapper.classList.add('cs-open')
        this.trigger.setAttribute('aria-expanded', 'true')

        requestAnimationFrame(() => {
            const rect  = this.wrapper.getBoundingClientRect()
            const below = window.innerHeight - rect.bottom
            const above = rect.top
            if (below < 240 && above > below) {
                this.dropdown.style.top    = 'auto'
                this.dropdown.style.bottom = '100%'
            } else {
                this.dropdown.style.top    = '100%'
                this.dropdown.style.bottom = 'auto'
            }
            this._searchInput?.focus()
            this.dropdown.querySelector('.cs-selected')?.scrollIntoView({ block: 'nearest' })
        })
    }

    _close() {
        this.wrapper.classList.remove('cs-open')
        this.trigger.setAttribute('aria-expanded', 'false')
        this._searchInput = null
    }

    _focusItem(index) {
        const items = this._getVisibleOptions()
        if (!items.length) { this._searchInput?.focus(); return }
        const target = index === -1 ? items.at(-1) : items[0]
        target?.focus()
    }

    _patchValueSetter() {
        const sel = this.select
        const dV  = Object.getOwnPropertyDescriptor(HTMLSelectElement.prototype, 'value')
        const dI  = Object.getOwnPropertyDescriptor(HTMLSelectElement.prototype, 'selectedIndex')
        Object.defineProperty(sel, 'value', {
            get: ()  => dV.get.call(sel),
            set: (v) => { dV.set.call(sel, v); this._syncDisplay() },
            configurable: true
        })
        Object.defineProperty(sel, 'selectedIndex', {
            get: ()  => dI.get.call(sel),
            set: (v) => { dI.set.call(sel, v); this._syncDisplay() },
            configurable: true
        })
    }

    _patchOptions() {
        const desc = Object.getOwnPropertyDescriptor(HTMLOptionElement.prototype, 'selected')
        Array.from(this.select.options).forEach(opt => {
            if (opt._csPatchedSelected) return
            opt._csPatchedSelected = true
            Object.defineProperty(opt, 'selected', {
                get: ()  => desc.get.call(opt),
                set: (v) => { desc.set.call(opt, v); this._syncDisplay() },
                configurable: true
            })
        })
    }

    _observe() {
        new MutationObserver(mutations => {
            let childChanged = false
            let attrChanged  = false
            for (const m of mutations) {
                if (m.type === 'childList')  childChanged = true
                if (m.type === 'attributes') attrChanged  = true
            }
            if (childChanged) this._patchOptions()
            if (childChanged || attrChanged) {
                this._syncDisplay()
                if (this.wrapper.classList.contains('cs-open')) this._buildOptions()
            }
        }).observe(this.select, {
            childList: true,
            attributes: true,
            attributeFilter: ['class']
        })
    }
}
