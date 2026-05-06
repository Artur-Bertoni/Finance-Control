/**
 * Helpers de Manipulação do DOM
 * Funções utilitárias que simplificam operações comuns
 * Torna o código menos "robotizado" e mais legível
 */

/**
 * Seleciona um elemento e lança erro se não encontrado
 * @param {string} selector - Seletor CSS
 * @param {string} context - Contexto (para mensagens de erro)
 * @returns {Element}
 */
export function required(selector, context = '') {
    const el = document.querySelector(selector)
    if (!el) {
        throw new Error(`Elemento não encontrado: ${selector}` + (context ? ` (em ${context})` : ''))
    }
    return el
}

/**
 * Seleciona múltiplos elementos
 * @param {string} selector - Seletor CSS
 * @returns {NodeList}
 */
export function queryAll(selector) {
    return document.querySelectorAll(selector)
}

/**
 * Obtém um elemento por ID com segurança
 * @param {string} id - ID do elemento
 * @param {string} context - Contexto (para mensagens de erro)
 * @returns {Element}
 */
export function getById(id, context = '') {
    return required(`#${id}`, context)
}

/**
 * Limpa o conteúdo de um elemento
 * @param {Element|string} element - Elemento ou seletor
 */
export function clear(element) {
    const el = typeof element === 'string' ? document.querySelector(element) : element
    if (el) el.innerHTML = ''
}

/**
 * Define o valor de um input com segurança
 * @param {string} elementId - ID do elemento
 * @param {any} value - Valor a definir
 */
export function setValue(elementId, value) {
    try {
        const element = document.getElementById(elementId)
        if (element) {
            element.value = value ?? ''
        }
    } catch (e) {
        console.warn(`Erro ao definir valor em ${elementId}:`, e)
    }
}

/**
 * Obtém o valor de um input com segurança
 * @param {string} elementId - ID do elemento
 * @returns {string}
 */
export function getValue(elementId) {
    try {
        const element = document.getElementById(elementId)
        return element?.value ?? ''
    } catch (e) {
        console.warn(`Erro ao obter valor de ${elementId}:`, e)
        return ''
    }
}

/**
 * Adiciona uma classe a um elemento
 * @param {Element|string} element - Elemento ou seletor
 * @param {string} className - Nome da classe
 */
export function addClass(element, className) {
    const el = typeof element === 'string' ? document.querySelector(element) : element
    if (el) el.classList.add(className)
}

/**
 * Remove uma classe de um elemento
 * @param {Element|string} element - Elemento ou seletor
 * @param {string} className - Nome da classe
 */
export function removeClass(element, className) {
    const el = typeof element === 'string' ? document.querySelector(element) : element
    if (el) el.classList.remove(className)
}

/**
 * Alterna uma classe em um elemento
 * @param {Element|string} element - Elemento ou seletor
 * @param {string} className - Nome da classe
 */
export function toggleClass(element, className) {
    const el = typeof element === 'string' ? document.querySelector(element) : element
    if (el) el.classList.toggle(className)
}

/**
 * Verifica se um elemento tem uma classe
 * @param {Element|string} element - Elemento ou seletor
 * @param {string} className - Nome da classe
 * @returns {boolean}
 */
export function hasClass(element, className) {
    const el = typeof element === 'string' ? document.querySelector(element) : element
    return el?.classList.contains(className) ?? false
}

/**
 * Define múltiplos atributos em um elemento
 * @param {Element|string} element - Elemento ou seletor
 * @param {Object} attributes - Objeto com pares atributo-valor
 */
export function setAttributes(element, attributes) {
    const el = typeof element === 'string' ? document.querySelector(element) : element
    if (!el) return
    Object.entries(attributes).forEach(([key, value]) => {
        el.setAttribute(key, value)
    })
}

/**
 * Remove atributos de um elemento
 * @param {Element|string} element - Elemento ou seletor
 * @param {string[]} attributes - Nomes dos atributos
 */
export function removeAttributes(element, attributes) {
    const el = typeof element === 'string' ? document.querySelector(element) : element
    if (!el) return
    attributes.forEach(attr => el.removeAttribute(attr))
}

/**
 * Adiciona um listener de evento com suporte a delegação
 * @param {string} selector - Seletor CSS
 * @param {string} eventType - Tipo de evento (ex: 'click', 'change')
 * @param {Function} handler - Função handler
 */
export function on(selector, eventType, handler) {
    const elements = document.querySelectorAll(selector)
    elements.forEach(el => {
        el.addEventListener(eventType, handler)
    })
}

/**
 * Valida um formulário simples
 * @param {Object} values - Objeto com os valores
 * @param {string[]} requiredFields - Campos obrigatórios
 * @returns {Object} { isValid: boolean, missingFields: string[] }
 */
export function validateForm(values, requiredFields) {
    const missingFields = requiredFields.filter(field => !values[field])
    return {
        isValid: missingFields.length === 0,
        missingFields
    }
}

/**
 * Desabilita um botão e adiciona estado de loading
 * @param {string} buttonId - ID do botão
 * @param {string} loadingText - Texto durante o loading
 */
export function disableButton(buttonId, loadingText = 'Carregando...') {
    const button = document.getElementById(buttonId)
    if (!button) return
    button.disabled = true
    button.dataset.originalText = button.textContent
    button.textContent = loadingText
}

/**
 * Reabilita um botão removendo estado de loading
 * @param {string} buttonId - ID do botão
 */
export function enableButton(buttonId) {
    const button = document.getElementById(buttonId)
    if (!button) return
    button.disabled = false
    const originalText = button.dataset.originalText
    if (originalText) {
        button.textContent = originalText
        delete button.dataset.originalText
    }
}

/**
 * Executa uma função quando o DOM está pronto
 * @param {Function} callback - Função a executar
 */
export function onReady(callback) {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', callback)
    } else {
        callback()
    }
}
