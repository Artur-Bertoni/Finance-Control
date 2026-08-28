const _EARS = `<path d="M80 24 C76 10 66 8 64 17 C62 27 71 32 80 24 Z" fill="var(--finny)" stroke="var(--finny-line)" stroke-width="2.5" stroke-linejoin="round"/><path d="M76.5 23 C74 14.5 69 13.5 67.5 18 C66 24 72 27 76.5 23 Z" fill="var(--finny-shade)"/><path d="M96 24 C100 10 110 8 112 17 C114 27 105 32 96 24 Z" fill="var(--finny)" stroke="var(--finny-line)" stroke-width="2.5" stroke-linejoin="round"/><path d="M99.5 23 C102 14.5 107 13.5 108.5 18 C110 24 104 27 99.5 23 Z" fill="var(--finny-shade)"/>`

const _FEATURES = `<ellipse cx="73" cy="51" rx="5" ry="3.2" fill="var(--finny-line)" opacity="0.28"/><ellipse cx="103" cy="52" rx="5" ry="3.2" fill="var(--finny-line)" opacity="0.28"/><circle cx="79" cy="40" r="4" fill="#3D2A38"/><circle cx="80.2" cy="38.7" r="1.2" fill="#fff"/><circle cx="97" cy="40" r="4" fill="#3D2A38"/><circle cx="98.3" cy="38.5" r="1.4" fill="#fff"/><ellipse cx="89" cy="54" rx="11.5" ry="7.5" fill="var(--finny-shade)" stroke="var(--finny-line)" stroke-width="2.5"/><ellipse cx="84.8" cy="54" rx="2" ry="3" fill="var(--finny-ink)"/><ellipse cx="93.2" cy="54" rx="2" ry="3" fill="var(--finny-ink)"/><path d="M84 64.5 Q89 68 94 64.5" fill="none" stroke="var(--finny-ink)" stroke-width="2.2" stroke-linecap="round"/>`

const _INNER = `<path d="M14 53 C5 52 3 42 10 40 C15.5 38.6 16.5 46.5 11 46" fill="none" stroke="var(--finny-line)" stroke-width="4" stroke-linecap="round"/><rect x="24" y="70" width="12.5" height="21" rx="6.25" fill="var(--finny-shade)" stroke="var(--finny-line)" stroke-width="2.5"/><rect x="60" y="70" width="12.5" height="21" rx="6.25" fill="var(--finny-shade)" stroke="var(--finny-line)" stroke-width="2.5"/><circle cx="44" cy="19" r="8" fill="var(--finny-coin)" stroke="var(--finny-coin-line)" stroke-width="2"/><circle cx="44" cy="19" r="3.6" fill="none" stroke="var(--finny-coin-line)" stroke-width="1.5" opacity="0.55"/><path d="M30 33 C42 24 52 23 58 28 C63 21 72 19 86 19 C100 19 112 29 112 45 C112 61 102 71 88 71 C82 71 77 71 74 68 C74 73 71 78.5 67 80 L67 86.5 A6.5 6.5 0 0 1 54 86.5 L54 80.5 C48 82 40 82 32 81 L32 86.5 A6.5 6.5 0 0 1 19 86.5 L19 79.5 C12 74 11 46 21 37 C24 34.5 27 33 30 33 Z" fill="var(--finny)" stroke="var(--finny-line)" stroke-width="2.5" stroke-linejoin="round"/><ellipse cx="46" cy="70" rx="23" ry="7.5" fill="#fff" opacity="0.18"/><rect x="-11" y="-3.2" width="22" height="6.4" rx="3.2" fill="var(--finny-ink)" transform="translate(44 29) rotate(-8)"/>${_EARS}${_FEATURES}`

const _FACE_INNER = `<ellipse cx="87" cy="45" rx="25" ry="26" fill="var(--finny)" stroke="var(--finny-line)" stroke-width="2.5"/>${_EARS}${_FEATURES}`

export class FinnySvg {
    static svg(cssClass = '') {
        const cls = cssClass ? ` class="${cssClass}"` : ''
        return `<svg${cls} viewBox="0 0 120 100" fill="none" xmlns="http://www.w3.org/2000/svg">${_INNER}</svg>`
    }

    static faceSvg(cssClass = '') {
        const cls = cssClass ? ` class="${cssClass}"` : ''
        return `<svg${cls} viewBox="58 6 58 68" fill="none" xmlns="http://www.w3.org/2000/svg">${_FACE_INNER}</svg>`
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
