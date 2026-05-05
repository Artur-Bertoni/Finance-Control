/**
 * Componente de Input de Senha com Toggle de Visibilidade
 * Substitui a lógica repetida de mostrar/ocultar senha em vários lugares
 */

import { Icons } from '../icons/IconLibrary.js'

export class PasswordInput {
    /**
     * Inicializa o toggle de visibilidade para um input de senha
     * @param {string} inputId - ID do input de senha
     * @param {string} buttonId - ID do botão de toggle
     */
    static setupToggle(inputId, buttonId) {
        const input = document.getElementById(inputId)
        const button = document.getElementById(buttonId)

        if (!input || !button) {
            console.warn(`PasswordInput: não encontrado input="${inputId}" ou button="${buttonId}"`)
            return
        }

        // Configurar o HTML do botão com os ícones
        button.type = 'button'
        button.setAttribute('aria-label', 'Mostrar/ocultar senha')
        
        // Garantir que o ícone seja renderizado
        const iconSvg = Icons.eyeOpen()
        if (!iconSvg) {
            console.error('Erro ao carregar ícone eyeOpen')
            return
        }
        button.innerHTML = iconSvg

        // Handler do toggle
        button.addEventListener('click', (event) => {
            event.preventDefault()
            event.stopPropagation()
            PasswordInput.toggleVisibility(input, button)
        })

        // Iniciar como escondido
        PasswordInput.setHidden(input, button)
    }

    /**
     * Alterna entre mostrar e esconder a senha
     */
    static toggleVisibility(input, button) {
        if (input.type === 'password') {
            PasswordInput.setVisible(input, button)
        } else {
            PasswordInput.setHidden(input, button)
        }
    }

    /**
     * Faz a senha ficar visível
     */
    static setVisible(input, button) {
        input.type = 'text'
        const iconSvg = Icons.eyeClosed()
        if (iconSvg) {
            button.innerHTML = iconSvg
        }
        button.setAttribute('aria-label', 'Ocultar senha')
    }

    /**
     * Faz a senha ficar escondida
     */
    static setHidden(input, button) {
        input.type = 'password'
        const iconSvg = Icons.eyeOpen()
        if (iconSvg) {
            button.innerHTML = iconSvg
        }
        button.setAttribute('aria-label', 'Mostrar senha')
    }
}
