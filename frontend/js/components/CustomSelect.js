export class CustomSelect {
    constructor(select) {
        this.select   = select
        this.wrapper  = null
        this.trigger  = null
        this.dropdown = null
        this._build()
        this._patchValueSetter()
        this._patchOptions()
        this._observe()
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
            if (e.key === 'ArrowDown') { e.preventDefault(); this._open(); this._focusItem(0) }
            if (e.key === 'ArrowUp')   { e.preventDefault(); this._open(); this._focusItem(-1) }
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
        this.trigger.textContent = opt ? opt.text : ''
        this.trigger.classList.toggle('cs-placeholder', isEmpty)
        this.trigger.classList.toggle('field-error', sel.classList.contains('field-error'))
    }

    _buildOptions() {
        const sel = this.select
        this.dropdown.innerHTML = ''
        Array.from(sel.options).forEach(opt => {
            if (opt.disabled) return
            const item = document.createElement('div')
            item.className = 'cs-option' + (opt.selected ? ' cs-selected' : '')
            item.textContent = opt.text
            item.tabIndex = -1
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
                if (e.key === 'ArrowDown') { e.preventDefault(); item.nextElementSibling?.focus() }
                if (e.key === 'ArrowUp')   {
                    e.preventDefault()
                    const prev = item.previousElementSibling
                    if (prev) prev.focus(); else this.trigger.focus()
                }
                if (e.key === 'Tab') this._close()
            })
            this.dropdown.appendChild(item)
        })
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
            this.dropdown.querySelector('.cs-selected')?.scrollIntoView({ block: 'nearest' })
        })
    }

    _close() {
        this.wrapper.classList.remove('cs-open')
        this.trigger.setAttribute('aria-expanded', 'false')
    }

    _focusItem(index) {
        const items = [...this.dropdown.querySelectorAll('.cs-option')]
        if (!items.length) return
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
