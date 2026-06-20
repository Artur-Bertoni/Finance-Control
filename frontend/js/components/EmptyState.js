export function createEmptyState(iconHtml, message) {
    const el = document.createElement('div')
    el.className = 'empty-state'
    el.style.gridColumn = '1 / -1'
    el.innerHTML = `${iconHtml}<p>${message}</p>`
    return el
}
