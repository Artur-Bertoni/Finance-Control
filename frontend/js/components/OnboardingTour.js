import { I18n } from '../i18n.js'
import { FinnySvg } from '../utils/FinnySvg.js'
import { UserSettings } from '../utils/UserSettings.js'
import { FEATURE_ROWS, PROFILE_PRESETS } from '../utils/FeatureCatalog.js'
import { SidebarManager } from './SidebarManager.js'

const ALL_STEPS = [
    { route: '/pages/HomePage.html',         target: '.sidebar-link[href="/pages/HomePage.html"]',       textKey: 'tour_home',      sidebar: true },
    { route: '/pages/Dashboard.html',        target: '.sidebar-link[href="/pages/Dashboard.html"]',      textKey: 'tour_dashboard', sidebar: true },
    { route: '/pages/Budget.html',           target: '.sidebar-link[href="/pages/Budget.html"]',         textKey: 'tour_budget',    sidebar: true, setting: 'budgetsEnabled' },
    { route: '/pages/lists/GoalList.html',   target: '.sidebar-link[href="/pages/lists/GoalList.html"]', textKey: 'tour_goals',     sidebar: true, setting: 'goalsEnabled' },
    { route: '/pages/FinnyCenter.html',      target: '#notifications-link',                               textKey: 'tour_finny',     sidebar: true, setting: 'finnyEnabled' },
    { route: '/pages/crud/Transaction.html', target: '#account-input',  textKey: 'tour_txAccount' },
    { target: '#category-input', textKey: 'tour_txCategory' },
    { target: '.radio-group',    textKey: 'tour_txType' },
    { target: '#value-input',    textKey: 'tour_txValue' },
    { target: '#save-btn',       textKey: 'tour_txSave' },
    { route: '/pages/views/UserView.html', target: '.sidebar-footer .sidebar-link[href="/pages/views/UserView.html"]', textKey: 'tour_profileSettings', sidebar: true },
]

export class OnboardingTour {

    static isDone() {
        return globalThis.__currentUser?.onboardingCompleted === true
    }

    static markDone() {
        if (OnboardingTour.isDone()) return
        if (globalThis.__currentUser) globalThis.__currentUser.onboardingCompleted = true
        $.ajax({ url: '/api/auth/onboarding/complete', type: 'POST', async: true, error: () => {} })
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
        this._steps = ALL_STEPS
        this._buildDom()
        this._renderProfileChoice()
    }

    /** Primeira etapa: o usuario escolhe um perfil de uso e o tour se ajusta ao que ficou ativo. */
    static _renderProfileChoice() {
        this._choosing = true
        this._currentTarget = null
        this._spotlight.hidden = true
        this._blocker.classList.add('tour-blocker--dim')
        this._bubble.classList.add('tour-bubble--center', 'tour-bubble--choice')
        this._dotsEl.innerHTML = ''
        this._backBtn.style.visibility = 'hidden'
        this._nextBtn.hidden = true

        this._textEl.textContent = I18n.t('onboardingProfileQuestion')

        const choices = document.createElement('div')
        choices.className = 'tour-profiles'
        for (const id of ['simple', 'complete', 'custom']) {
            const card = document.createElement('button')
            card.type = 'button'
            card.className = 'tour-profile-card'
            card.innerHTML = `
                <span class="tour-profile-name">${I18n.t(`onboardingProfile_${id}`)}</span>
                <span class="tour-profile-desc">${I18n.t(`onboardingProfile_${id}_desc`)}</span>
            `
            card.addEventListener('click', () => {
                if (id === 'custom') this._renderCustomProfile()
                else this._applyProfile(PROFILE_PRESETS[id])
            })
            choices.appendChild(card)
        }

        this._textEl.insertAdjacentElement('afterend', choices)
        this._choiceEl = choices
        this._positionFor(null)
    }

    static _renderCustomProfile() {
        this._choiceEl?.remove()
        this._textEl.textContent = I18n.t('onboardingProfileCustomIntro')

        const list = document.createElement('div')
        list.className = 'tour-profile-toggles'
        const inputs = {}

        for (const row of FEATURE_ROWS) {
            const label = document.createElement('label')
            label.className = 'tour-profile-toggle'
            const title = I18n.t(row.titleKey)
            label.innerHTML = `
                <input type="checkbox" checked aria-label="${title}">
                <span class="tour-profile-toggle-text">
                    <span class="tour-profile-toggle-name">${title}</span>
                    <span class="tour-profile-toggle-desc">${I18n.t(row.descKey)}</span>
                </span>
            `
            inputs[row.key] = label.querySelector('input')
            list.appendChild(label)
        }

        this._textEl.insertAdjacentElement('afterend', list)
        this._choiceEl = list

        this._nextBtn.hidden = false
        this._nextBtn.textContent = I18n.t('onboardingContinue')
        this._nextBtn.onclick = () => {
            const chosen = {}
            for (const [key, input] of Object.entries(inputs)) chosen[key] = input.checked
            this._applyProfile(chosen)
        }
        this._positionFor(null)
    }

    static _applyProfile(settings) {
        this._nextBtn.disabled = true

        $.ajax({
            url:         '/api/user-settings',
            type:        'PUT',
            contentType: 'application/json',
            data:        JSON.stringify(settings),
            complete: xhr => {
                if (xhr.status >= 200 && xhr.status < 300 && xhr.responseJSON) {
                    UserSettings.store(xhr.responseJSON)
                    SidebarManager.applyFeatureVisibility()
                    SidebarManager.applyFinnyLink()
                }
                this._startGuidedSteps()
            }
        })
    }

    static _startGuidedSteps() {
        this._choosing = false
        this._choiceEl?.remove()
        this._choiceEl = null
        this._bubble.classList.remove('tour-bubble--choice')
        this._nextBtn.hidden   = false
        this._nextBtn.disabled = false
        this._nextBtn.onclick  = null
        this._applyFinnyBranding()

        this._steps = ALL_STEPS.filter(s => !s.setting || UserSettings.isEnabled(s.setting))
        this._goToStep(0)
    }

    static _applyFinnyBranding() {
        if (UserSettings.finny) return
        this._bubble.querySelector('.tour-finny')?.remove()
        const name = this._bubble.querySelector('.tour-bubble-name')
        if (name) name.textContent = 'Finance Control'
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
            if (this._choosing) return
            if (this._step >= this._steps.length - 1) this._finish()
            else this._goToStep(this._step + 1)
        })

        this._reposition = () => this._positionFor(this._currentTarget)
        globalThis.addEventListener('resize', this._reposition)
        globalThis.addEventListener('scroll', this._reposition, true)
    }

    static async _goToStep(i) {
        if (this._busy || i < 0 || i >= this._steps.length) return
        this._busy = true
        this._step = i
        const step = this._steps[i]
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
        const step = this._steps[this._step]
        this._textEl.textContent = I18n.t(step.textKey)
        this._dotsEl.innerHTML = this._steps
            .map((_, i) => `<span class="tour-dot${i === this._step ? ' tour-dot--active' : ''}"></span>`)
            .join('')
        this._backBtn.style.visibility = this._step === 0 ? 'hidden' : 'visible'
        this._nextBtn.textContent = this._step >= this._steps.length - 1
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
