const _INNER = `<path d="M15 52 C4 52, 4 38, 15 38" stroke="#fda4c0" stroke-width="4.5" stroke-linecap="round"/><ellipse cx="55" cy="54" rx="34" ry="28" fill="#fdb8ce"/><rect x="41" y="23" width="18" height="5" rx="2.5" fill="#e87aaa"/><ellipse cx="79" cy="27" rx="8" ry="11" fill="#fdb8ce"/><ellipse cx="79" cy="28.5" rx="4.5" ry="7" fill="#f9a0c0"/><circle cx="84" cy="46" r="22" fill="#fdb8ce"/><circle cx="91" cy="38" r="4" fill="white"/><circle cx="92" cy="38" r="2" fill="#1a1a2e"/><circle cx="92.8" cy="37.2" r="0.8" fill="white"/><ellipse cx="101" cy="51" rx="9.5" ry="7" fill="#f9a0c0"/><circle cx="98" cy="51" r="2.5" fill="#d4608a"/><circle cx="104" cy="51" r="2.5" fill="#d4608a"/><path d="M95 57 Q101 63 107 57" stroke="#d4608a" stroke-width="2.5" stroke-linecap="round"/><rect x="28" y="77" width="11" height="12" rx="5.5" fill="#fdb8ce"/><rect x="43" y="80" width="11" height="12" rx="5.5" fill="#fdb8ce"/><rect x="58" y="80" width="11" height="12" rx="5.5" fill="#fdb8ce"/><rect x="73" y="77" width="11" height="12" rx="5.5" fill="#fdb8ce"/>`

const _FACE_INNER = `<ellipse cx="79" cy="27" rx="8" ry="11" fill="#fdb8ce"/><ellipse cx="79" cy="28.5" rx="4.5" ry="7" fill="#f9a0c0"/><circle cx="84" cy="46" r="22" fill="#fdb8ce"/><circle cx="91" cy="38" r="4" fill="white"/><circle cx="92" cy="38" r="2" fill="#1a1a2e"/><circle cx="92.8" cy="37.2" r="0.8" fill="white"/><ellipse cx="101" cy="51" rx="9.5" ry="7" fill="#f9a0c0"/><circle cx="98" cy="51" r="2.5" fill="#d4608a"/><circle cx="104" cy="51" r="2.5" fill="#d4608a"/><path d="M95 57 Q101 63 107 57" stroke="#d4608a" stroke-width="2.5" stroke-linecap="round"/>`

export class FinnySvg {
    static svg(cssClass = '') {
        const cls = cssClass ? ` class="${cssClass}"` : ''
        return `<svg${cls} viewBox="0 0 120 100" fill="none" xmlns="http://www.w3.org/2000/svg">${_INNER}</svg>`
    }

    static faceSvg(cssClass = '') {
        const cls = cssClass ? ` class="${cssClass}"` : ''
        return `<svg${cls} viewBox="58 14 54 62" fill="none" xmlns="http://www.w3.org/2000/svg">${_FACE_INNER}</svg>`
    }

    static autoInit(root = document) {
        root.querySelectorAll('[data-finny-svg]').forEach(el => {
            const cssClass = el.dataset.finnySvg
            const isFace = 'finnyFace' in el.dataset
            const tpl = document.createElement('template')
            tpl.innerHTML = isFace ? FinnySvg.faceSvg(cssClass) : FinnySvg.svg(cssClass)
            el.replaceWith(tpl.content.firstChild)
        })
    }
}
