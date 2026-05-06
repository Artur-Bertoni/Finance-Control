const SVG_UP   = `<svg viewBox="0 0 10 6" fill="none" xmlns="http://www.w3.org/2000/svg"><polyline points="1,5 5,1 9,5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>`
const SVG_DOWN = `<svg viewBox="0 0 10 6" fill="none" xmlns="http://www.w3.org/2000/svg"><polyline points="1,1 5,5 9,1" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>`

export class NumberSpinner {
    static autoInit() {
        document.querySelectorAll('input[type="number"]:not([data-spinner-init])').forEach(el => NumberSpinner.wrap(el))
        document.querySelectorAll('[data-mask="money"]:not([data-spinner-init])').forEach(el => NumberSpinner.wrap(el, { step: 1, min: 0 }))
    }

    static wrap(input, opts = {}) {
        if (input.dataset.spinnerInit) return
        input.dataset.spinnerInit = '1'

        const step   = opts.step ?? (Number.parseFloat(input.step) || 1)
        const getMax = () => opts.max ?? (input.max === '' ? Infinity  : Number.parseFloat(input.max))
        const getMin = () => opts.min ?? (input.min === '' ? -Infinity : Number.parseFloat(input.min))

        const wrapper = document.createElement('div')
        wrapper.className = 'number-spinner-wrap'
        input.parentNode.insertBefore(wrapper, input)
        wrapper.appendChild(input)

        const upBtn   = NumberSpinner._btn(SVG_UP,   'spin-up')
        const downBtn = NumberSpinner._btn(SVG_DOWN, 'spin-down')

        const btns = document.createElement('div')
        btns.className = 'number-spin-btns'
        btns.appendChild(upBtn)
        btns.appendChild(downBtn)
        wrapper.appendChild(btns)

        upBtn.addEventListener('mousedown', e => {
            e.preventDefault()
            const val = Math.min((Number.parseFloat(input.value) || 0) + step, getMax())
            input.value = +val.toFixed(10)
            input.dispatchEvent(new Event('input',  { bubbles: true }))
            input.dispatchEvent(new Event('change', { bubbles: true }))
        })

        downBtn.addEventListener('mousedown', e => {
            e.preventDefault()
            const val = Math.max((Number.parseFloat(input.value) || 0) - step, getMin())
            input.value = +val.toFixed(10)
            input.dispatchEvent(new Event('input',  { bubbles: true }))
            input.dispatchEvent(new Event('change', { bubbles: true }))
        })
    }

    static _btn(svg, cls) {
        const btn = document.createElement('button')
        btn.type      = 'button'
        btn.className = `spin-btn ${cls}`
        btn.tabIndex  = -1
        btn.innerHTML = svg
        return btn
    }
}
