/**
 * Adiciona um overlay de carregamento em tela cheia ao body.
 * @returns {HTMLElement} o elemento overlay — chame .remove() quando finalizar.
 */
export function showOverlay() {
    const overlay = document.createElement('div')
    overlay.className = 'loading-overlay'
    overlay.innerHTML = '<div class="loading-spinner"></div>'
    document.body.appendChild(overlay)
    return overlay
}
