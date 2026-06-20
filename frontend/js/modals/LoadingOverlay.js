export function showOverlay() {
    const overlay = document.createElement('div')
    overlay.className = 'loading-overlay'
    overlay.innerHTML = '<div class="loading-spinner"></div>'
    document.body.appendChild(overlay)
    return overlay
}
