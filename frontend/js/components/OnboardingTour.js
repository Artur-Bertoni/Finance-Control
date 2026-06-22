import { I18n } from '../i18n.js'
import { FinnySvg } from '../utils/FinnySvg.js'

const STEPS = [
    { route: '/pages/HomePage.html',         target: '.sidebar-link[href="/pages/HomePage.html"]',       textKey: 'tour_home',      sidebar: true },
    { route: '/pages/Dashboard.html',        target: '.sidebar-link[href="/pages/Dashboard.html"]',      textKey: 'tour_dashboard', sidebar: true },
    { route: '/pages/Budget.html',           target: '.sidebar-link[href="/pages/Budget.html"]',         textKey: 'tour_budget',    sidebar: true },
    { route: '/pages/lists/GoalList.html',   target: '.sidebar-link[href="/pages/lists/GoalList.html"]', textKey: 'tour_goals',     sidebar: true },
    { route: '/pages/FinnyCenter.html',      target: '#notifications-link',                               textKey: 'tour_finny',     sidebar: true },
    { route: '/pages/crud/Transaction.html', target: '#account-input',  textKey: 'tour_txAccount' },
    { target: '#category-input', textKey: 'tour_txCategory' },
    { target: '.radio-group',    textKey: 'tour_txType' },
    { target: '#value-input',    textKey: 'tour_txValue' },
    { target: '#save-btn',       textKey: 'tour_txSave' },
]

export class OnboardingTour {

    static _storageKey() {
        const uid = globalThis.__currentUser?.id ?? 'anon'
        return `finny_onboarding_done_${uid}`
    }

    static isDone() {
        try { return localStorage.getItem(OnboardingTour._storageKey()) === '1' } catch { return false }
    }

    static markDone() {
        try { localStorage.setItem(OnboardingTour._storageKey(), '1') } catch { }
    }

    static maybeStart() {
        if (!globalThis.__currentUser) return
        if (OnboardingTour.isDone()) return
        if (!location.pathname.endsWith('/HomePage.html')) return
        OnboardingTour.start()
    }

    static start() {
        if (this._running) return
        this._running = true
        this._step = 0
        this._busy = false
        this._buildDom()
        this._goToStep(0)
    }

    static _buildDom() {
        const overlay = document.createElement('div')
        overlay.className = 'tour-overlay'
        overlay.innerHTML = `
            <div class="tour-blocker"></div>
            <div class="tour-spotlight" hidden></div>
            <div class="tour-bubble">
                <button class="tour-skip" type="button">${I18n.t('onboardingSkip')}</button>
                <div class="tour-bubble-head">
                    <span class="tour-finny">${FinnySvg.faceSvg('tour-finny-svg')}</span>
                    <span class="tour-bubble-name">Finny</span>
                </div>
                <p class="tour-text"></p>
                <div class="tour-dots"></div>
                <div class="tour-actions">
                    <button class="btn btn-secondary btn-sm tour-back" type="button">${I18n.t('onboardingBack')}</button>
                    <button class="btn btn-primary btn-sm tour-next" type="button"></button>
                </div>
            </div>
        `
        document.body.appendChild(overlay)

        this._overlay   = overlay
        this._blocker   = overlay.querySelector('.tour-blocker')
        this._spotlight = overlay.querySelector('.tour-spotlight')
        this._bubble    = overlay.querySelector('.tour-bubble')
        this._textEl    = overlay.querySelector('.tour-text')
        this._dotsEl    = overlay.querySelector('.tour-dots')
        this._backBtn   = overlay.querySelector('.tour-back')
        this._nextBtn   = overlay.querySelector('.tour-next')

        overlay.querySelector('.tour-skip').addEventListener('click', () => this._finish())
        this._backBtn.addEventListener('click', () => this._goToStep(this._step - 1))
        this._nextBtn.addEventListener('click', () => {
            if (this._step >= STEPS.length - 1) this._finish()
            else this._goToStep(this._step + 1)
        })

        this._reposition = () => this._positionFor(this._currentTarget)
        globalThis.addEventListener('resize', this._reposition)
        globalThis.addEventListener('scroll', this._reposition, true)
    }

    static async _goToStep(i) {
        if (this._busy || i < 0 || i >= STEPS.length) return
        this._busy = true
        this._step = i
        const step = STEPS[i]
        this._nextBtn.disabled = true
        this._backBtn.disabled = true

        const targetPage = step.route ? step.route.split('/').pop() : null
        if (targetPage && !location.pathname.endsWith(targetPage)) {
            try { await globalThis.__appRouter?.navigate(step.route) } catch { }
        }

        const toggled = this._syncSidebar(step.sidebar)
        await this._wait(toggled ? 320 : 90)

        let target = await this._waitFor(step.target)
        target = await this._resolveVisible(target)
        this._currentTarget = target
        if (target) {
            target.scrollIntoView({ block: 'center', inline: 'nearest' })
            await this._wait(60)
        }

        this._renderText()
        this._positionFor(target)

        this._nextBtn.disabled = false
        this._backBtn.disabled = false
        this._busy = false
    }

    static async _resolveVisible(el) {
        if (!el || el.tagName !== 'SELECT') return el
        for (let i = 0; i < 30; i++) {
            const trigger = el.closest('.cs-wrapper')?.querySelector('.cs-trigger')
            if (trigger) return trigger
            await this._wait(30)
        }
        return el
    }

    static _renderText() {
        const step = STEPS[this._step]
        this._textEl.textContent = I18n.t(step.textKey)
        this._dotsEl.innerHTML = STEPS
            .map((_, i) => `<span class="tour-dot${i === this._step ? ' tour-dot--active' : ''}"></span>`)
            .join('')
        this._backBtn.style.visibility = this._step === 0 ? 'hidden' : 'visible'
        this._nextBtn.textContent = this._step >= STEPS.length - 1
            ? I18n.t('onboardingFinish')
            : I18n.t('onboardingNext')
    }

    static _positionFor(target) {
        const vw = globalThis.innerWidth
        const vh = globalThis.innerHeight
        const rect = target ? target.getBoundingClientRect() : null
        const visible = !!rect && rect.width > 0 && rect.height > 0 &&
            rect.bottom > 0 && rect.top < vh && rect.left >= 0 && rect.right <= vw + 1

        if (visible) {
            const pad = 6
            this._spotlight.hidden = false
            this._spotlight.style.top    = `${rect.top - pad}px`
            this._spotlight.style.left   = `${rect.left - pad}px`
            this._spotlight.style.width  = `${rect.width + pad * 2}px`
            this._spotlight.style.height = `${rect.height + pad * 2}px`
            this._blocker.classList.remove('tour-blocker--dim')

            const bw = this._bubble.offsetWidth  || 340
            const bh = this._bubble.offsetHeight || 170
            let top
            if (vh - rect.bottom > bh + 16)      top = rect.bottom + 12
            else if (rect.top > bh + 16)         top = rect.top - bh - 12
            else                                 top = Math.max(12, (vh - bh) / 2)
            let left = rect.left + rect.width / 2 - bw / 2
            left = Math.min(Math.max(12, left), vw - bw - 12)

            this._bubble.classList.remove('tour-bubble--center')
            this._bubble.style.top  = `${top}px`
            this._bubble.style.left = `${left}px`
        } else {
            this._spotlight.hidden = true
            this._blocker.classList.add('tour-blocker--dim')
            this._bubble.classList.add('tour-bubble--center')
            this._bubble.style.top  = ''
            this._bubble.style.left = ''
        }
    }

    static _syncSidebar(show) {
        const sidebar = document.getElementById('sidebar')
        if (!sidebar) return false
        const isMobile = globalThis.matchMedia('(max-width: 768px)').matches
        if (!isMobile) return false
        const want = !!show
        if (want === sidebar.classList.contains('open')) return false
        sidebar.classList.toggle('open', want)
        return true
    }

    static _waitFor(selector, timeout = 3000) {
        return new Promise(resolve => {
            const start = performance.now()
            const tryFind = () => {
                const el = document.querySelector(selector)
                if (el) return resolve(el)
                if (performance.now() - start > timeout) return resolve(null)
                requestAnimationFrame(tryFind)
            }
            tryFind()
        })
    }

    static _wait(ms) {
        return new Promise(r => setTimeout(r, ms))
    }

    static _finish() {
        OnboardingTour.markDone()
        globalThis.removeEventListener('resize', this._reposition)
        globalThis.removeEventListener('scroll', this._reposition, true)
        this._syncSidebar(false)
        this._overlay?.remove()
        this._overlay = null
        this._currentTarget = null
        this._running = false
    }
}
