import { I18n } from '../i18n.js'

const MONEY_LOCALE_MAP = { pt: 'pt-BR', en: 'en-US', es: 'es-ES' }

function formatMoneyDisplay(num) {
    const locale = MONEY_LOCALE_MAP[I18n.getLanguage()] ?? 'pt-BR'
    return new Intl.NumberFormat(locale, { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(num)
}

export class InputMasks {
    static autoInit() {
        document.querySelectorAll('[data-mask="money"]').forEach(el => InputMasks.money(el))
        document.querySelectorAll('[data-mask="email"]').forEach(el => InputMasks.email(el))
    }

    static reformatAll() {
        document.querySelectorAll('[data-mask-init]').forEach(el => {
            if (el._reformatMoney) el._reformatMoney()
        })
    }

    static money(input) {
        if (input.dataset.maskInit) return
        input.dataset.maskInit = '1'

        const orig = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')
        let digits = ''

        const format = () =>
            orig.set.call(input, digits ? formatMoneyDisplay(Number.parseInt(digits, 10) / 100) : '')

        input._reformatMoney = format

        Object.defineProperty(input, 'value', {
            get: () => digits ? String(Number.parseInt(digits, 10) / 100) : '',
            set: (v) => {
                const n = Number.parseFloat(v)
                digits = (v === '' || v === null || v === undefined || Number.isNaN(n))
                    ? ''
                    : Math.round(Math.abs(n) * 100).toString()
                format()
            },
            configurable: true
        })

        input.addEventListener('keydown', e => {
            const allSelected = input.selectionStart === 0 && input.selectionEnd === input.value.length
            if (/^\d$/.test(e.key)) {
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
            const nums = (e.clipboardData || globalThis.clipboardData).getData('text').replaceAll(/\D/g, '')
            if (nums) digits = nums.slice(0, 12)
            format()
            input.dispatchEvent(new Event('input', { bubbles: true }))
        })
    }

    static email(input) {
        if (input.dataset.maskInit) return
        input.dataset.maskInit = '1'

        input.addEventListener('input', () => {
            const cleaned = input.value.replaceAll(/[^\w.@+-]/gi, '').toLowerCase()
            if (cleaned !== input.value) input.value = cleaned
        })

        input.addEventListener('paste', e => {
            e.preventDefault()
            const text = (e.clipboardData || globalThis.clipboardData).getData('text')
            const cleaned = text.replaceAll(/[^\w.@+-]/gi, '').toLowerCase().trim()
            document.execCommand('insertText', false, cleaned)
        })
    }
}
