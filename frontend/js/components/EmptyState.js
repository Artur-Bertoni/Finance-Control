/**
 * Creates the standard empty-state element used in list pages.
 * @param {string} iconHtml  - HTML string for the icon (e.g. Icons.categories())
 * @param {string} message   - Translated message string
 * @returns {HTMLElement}
 */
export function createEmptyState(iconHtml, message) {
    const el = document.createElement('div')
    el.className = 'empty-state'
    el.style.gridColumn = '1 / -1'
    el.innerHTML = `${iconHtml}<p>${message}</p>`
    return el
}
