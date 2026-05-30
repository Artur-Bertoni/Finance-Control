/**
 * Cria o elemento de estado vazio padrão usado nas páginas de lista.
 * @param {string} iconHtml  - HTML do ícone (ex: Icons.categories())
 * @param {string} message   - Texto da mensagem traduzido
 * @returns {HTMLElement}
 */
export function createEmptyState(iconHtml, message) {
    const el = document.createElement('div')
    el.className = 'empty-state'
    el.style.gridColumn = '1 / -1'
    el.innerHTML = `${iconHtml}<p>${message}</p>`
    return el
}
