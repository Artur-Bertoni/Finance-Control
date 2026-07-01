import { Icons } from '../icons/IconLibrary.js'
import { I18n } from '../i18n.js'

export class PasswordInput {
    static PW_RULES = {
        length:  v => v.length >= 12,
        upper:   v => /[A-Z]/.test(v),
        lower:   v => /[a-z]/.test(v),
        digit:   v => /\d/.test(v),
        special: v => /[^A-Za-z0-9]/.test(v),
    }

    static PW_RULE_KEYS = {
        length:  'pwReqLength',
        upper:   'pwReqUpper',
        lower:   'pwReqLower',
        digit:   'pwReqDigit',
        special: 'pwReqSpecial',
    }

    static requirementsHtml() {
        const items = Object.entries(PasswordInput.PW_RULE_KEYS).map(([rule, key]) =>
            `<li class="pw-req" data-rule="${rule}"><i class="ph ph-circle"></i><span data-i18n="${key}">${I18n.t(key)}</span></li>`
        ).join('')
        return `<ul class="pw-requirements">${items}</ul>`
    }

    static attachRequirements(input, listEl) {
        const items = listEl.querySelectorAll('.pw-req')
        const evaluate = () => {
            const value = input.value
            let allMet = true
            items.forEach(li => {
                const met = PasswordInput.PW_RULES[li.dataset.rule](value)
                li.classList.toggle('pw-req--met', met)
                const icon = li.querySelector('i')
                if (icon) icon.className = met ? 'ph ph-check-circle' : 'ph ph-circle'
                if (!met) allMet = false
            })
            return allMet
        }
        input.addEventListener('input', evaluate)
        evaluate()
        return { isValid: evaluate }
    }

    static setupToggle(inputId, buttonId) {
        const initialize = () => {
            const input = document.getElementById(inputId)
            const button = document.getElementById(buttonId)

            if (!input || !button) return

            button.type = 'button'
            button.setAttribute('aria-label', 'Mostrar/ocultar senha')

            const iconSvg = Icons.eyeOpen()
            if (!iconSvg) return
            button.innerHTML = iconSvg

            button.addEventListener('click', (event) => {
                event.preventDefault()
                event.stopPropagation()
                PasswordInput.toggleVisibility(input, button)
            })

            PasswordInput.setHidden(input, button)
        }

        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', initialize)
        } else {
            initialize()
        }
    }

    static toggleVisibility(input, button) {
        if (input.type === 'password') {
            PasswordInput.setVisible(input, button)
        } else {
            PasswordInput.setHidden(input, button)
        }
    }

    static setVisible(input, button) {
        input.type = 'text'
        const iconSvg = Icons.eyeClosed()
        if (iconSvg) button.innerHTML = iconSvg
        button.setAttribute('aria-label', 'Ocultar senha')
    }

    static setHidden(input, button) {
        input.type = 'password'
        const iconSvg = Icons.eyeOpen()
        if (iconSvg) button.innerHTML = iconSvg
        button.setAttribute('aria-label', 'Mostrar senha')
    }
}
