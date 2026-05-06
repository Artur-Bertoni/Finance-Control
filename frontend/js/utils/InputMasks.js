export class InputMasks {
    static autoInit() {
        document.querySelectorAll('[data-mask="money"]').forEach(el => InputMasks.money(el))
        document.querySelectorAll('[data-mask="email"]').forEach(el => InputMasks.email(el))
    }

    static money(input) {
        if (input.dataset.maskInit) return
        input.dataset.maskInit = '1'

        const orig = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')
        let digits = ''

        const format = () =>
            orig.set.call(input, digits ? (parseInt(digits, 10) / 100).toFixed(2) : '')

        Object.defineProperty(input, 'value', {
            get: () => orig.get.call(input),
            set: (v) => {
                const n = parseFloat(v)
                digits = (v === '' || v === null || v === undefined || isNaN(n))
                    ? ''
                    : Math.round(Math.abs(n) * 100).toString()
                format()
            },
            configurable: true
        })

        input.addEventListener('keydown', e => {
            const allSelected = input.selectionStart === 0 && input.selectionEnd === input.value.length
            if (/^[0-9]$/.test(e.key)) {
                e.preventDefault()
                if (allSelected) digits = ''
                if (digits.length < 12) digits += e.key
            } else if (e.key === 'Backspace') {
                e.preventDefault()
                digits = allSelected ? '' : digits.slice(0, -1)
            } else if (e.key === 'Delete') {
                e.preventDefault()
                digits = ''
            } else if (
                !e.ctrlKey && !e.metaKey &&
                !['Tab','Enter','Escape','Home','End','Shift','Alt','Control','Meta',
                  'ArrowLeft','ArrowRight','ArrowUp','ArrowDown'].includes(e.key) &&
                !/^F\d+$/.test(e.key)
            ) {
                e.preventDefault()
                return
            } else {
                return
            }
            format()
            input.dispatchEvent(new Event('input', { bubbles: true }))
        })

        input.addEventListener('paste', e => {
            e.preventDefault()
            const nums = (e.clipboardData || window.clipboardData).getData('text').replace(/\D/g, '')
            if (nums) digits = nums.slice(0, 12)
            format()
            input.dispatchEvent(new Event('input', { bubbles: true }))
        })
    }

    static email(input) {
        if (input.dataset.maskInit) return
        input.dataset.maskInit = '1'

        input.addEventListener('input', () => {
            const cleaned = input.value.replace(/[^\w.@+\-]/gi, '').toLowerCase()
            if (cleaned !== input.value) input.value = cleaned
        })

        input.addEventListener('paste', e => {
            e.preventDefault()
            const text = (e.clipboardData || window.clipboardData).getData('text')
            document.execCommand('insertText', false, text.replace(/[^\w.@+\-]/gi, '').toLowerCase().trim())
        })
    }
}
