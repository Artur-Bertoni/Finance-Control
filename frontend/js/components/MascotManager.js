import { doRequest, navigate, showToast } from '../../utils/FrontendFunctions.js'
import { I18n } from '../i18n.js'

// Dicas estáticas (genéricas). Usadas só como "recheio" do popup quando não há dica nova do agente.
const STATIC_TIPS = {
    pt: [
        'A regra 50/30/20 é simples: 50% para necessidades, 30% para desejos e 20% para poupança. Que tal tentar este mês?',
        'Reserva de emergência é prioridade! O ideal é ter de 3 a 6 meses de despesas guardadas.',
        'Revise suas assinaturas mensais. Você ainda usa todos os serviços que paga?',
        'Antes de uma compra por impulso, espere 24 horas. Você ainda vai querer aquilo amanhã?',
        'Juros compostos são maravilhosos! Quanto antes você começar a investir, mais seu dinheiro cresce.',
        'Diversifique seus investimentos! Não coloque todos os ovos na mesma cesta.',
        'Categorizar suas transações ajuda a entender onde seu dinheiro vai. Continue cadastrando!',
        'Monitorar seus gastos regularmente é o primeiro passo para a saúde financeira. Parabéns por estar aqui!',
        'Negocie dívidas! Pergunte sempre sobre condições especiais — desconto à vista pode chegar a 50%.',
        'Automatize sua poupança: transfira para uma reserva logo que o salário cair.',
    ],
    en: [
        'The 50/30/20 rule: 50% for needs, 30% for wants, and 20% for savings. Give it a try this month!',
        'Emergency fund first! Aim for 3 to 6 months of expenses saved up.',
        'Review your monthly subscriptions. Are you still using everything you pay for?',
        'Before an impulse buy, wait 24 hours. Will you still want it tomorrow?',
        'Compound interest is amazing! The earlier you start investing, the more your money grows.',
        'Diversify your investments! Don\'t put all your eggs in one basket.',
        'Categorizing your transactions helps you understand where your money goes. Keep it up!',
        'Monitoring your spending regularly is the first step to financial health. Glad you\'re here!',
        'Negotiate your debts! Always ask about special conditions — cash discounts can reach 50%.',
        'Automate your savings: transfer to a reserve right when your paycheck arrives.',
    ],
    es: [
        'La regla 50/30/20: 50% para necesidades, 30% para deseos y 20% para ahorro. ¡Pruébalo este mes!',
        '¡El fondo de emergencia es prioridad! Lo ideal es tener entre 3 y 6 meses de gastos ahorrados.',
        'Revisa tus suscripciones mensuales. ¿Todavía usas todos los servicios que pagas?',
        'Antes de una compra por impulso, espera 24 horas. ¿Todavía lo querrás mañana?',
        '¡El interés compuesto es maravilloso! Cuanto antes empieces a invertir, más crece tu dinero.',
        '¡Diversifica tus inversiones! No pongas todos los huevos en la misma canasta.',
        'Categorizar tus transacciones te ayuda a entender adónde va tu dinero. ¡Sigue haciéndolo!',
        'Monitorear tus gastos regularmente es el primer paso para la salud financiera. ¡Me alegra que estés aquí!',
        '¡Negocia tus deudas! Siempre pregunta por condiciones especiales — el descuento al contado puede llegar al 50%.',
        'Automatiza tu ahorro: transfiere a una reserva justo cuando llegue tu sueldo.',
    ],
}

const TYPE_META = {
    GOAL_MILESTONE_50:     { icon: '📊', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 50 } },
    GOAL_MILESTONE_75:     { icon: '📈', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 75 } },
    GOAL_MILESTONE_90:     { icon: '🔥', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 90 } },
    GOAL_COMPLETED:        { icon: '🎯', i18nKey: 'notifGoalCompleted'   },
    GOAL_EXCEEDED:         { icon: '⚠️', i18nKey: 'notifGoalExceeded'   },
    GOAL_DEADLINE_WARNING: { icon: '⏰', i18nKey: 'notifGoalDeadline'   },
}


// ── Dicas do agente (backend) ──────────────────────────────────────────────────
// O backend devolve dicas como dados (ruleKey + params + status); o texto é renderizado
// aqui, no idioma atual. Ciclo: NEW (a popar) → SHOWN (no modal) → feedback (sai do modal).

function renderTipText(tip) {
    return I18n.t('finnyTip_' + tip.ruleKey, tip.params ?? {})
}

function fetchAgentTips() {
    try { return doRequest('/api/finny/tips', 'GET') ?? [] } catch { return [] }
}

/** Dicas SHOWN (já mostradas em popup, aguardando feedback) viram itens do modal. */
function shownToItems(tips) {
    return (tips ?? [])
        .filter(t => t.status === 'SHOWN')
        .map(t => ({ id: t.id, text: renderTipText(t), severity: t.severity ?? 'info', feedbackable: true }))
}

function randomStatic(lang) {
    const pool = STATIC_TIPS[lang] ?? STATIC_TIPS.pt
    return pool.length ? pool[Math.floor(Math.random() * pool.length)] : ''
}

/** Clona o primeiro elemento de um <template> do HTML (definido no AppShell). */
function _clone(id) {
    return document.getElementById(id).content.firstElementChild.cloneNode(true)
}

function _notifDate(iso) {
    return new Date(iso).toLocaleString(I18n.getLanguage(), { dateStyle: 'short', timeStyle: 'short' })
}


export class MascotManager {
    static _items          = []        // itens do painel flutuante (dicas SHOWN aguardando feedback)
    static _index          = 0
    static _dashItems      = []        // itens do widget do dashboard
    static _dashIndex      = 0
    static _agentTipsRaw   = null      // cache das dicas cruas do agente (NEW + SHOWN)
    static _agentCacheTime = 0
    static _AGENT_TTL      = 5 * 60 * 1000
    static _poppedIds      = new Set() // ids já mostrados em popup nesta sessão (evita repetir)
    static _lastPopText    = ''        // último texto popado (evita estática repetida em sequência)
    static _currentTip     = ''
    static _nextTipTime    = 0
    static _countdownTimer = null
    static _activeTab      = 'tips'
    static _pushTimer      = null
    static _TIP_PUSH_MS    = 30 * 60 * 1000
    static _FIRST_POP_MS   = 5000
    static _STORAGE_TIP    = 'fc_finny_tip'
    static _STORAGE_NEXT   = 'fc_finny_next'

    static initFloating() {
        const fab      = document.getElementById('mascot-fab')
        const panel    = document.getElementById('mascot-panel')
        const closeBtn = document.getElementById('mascot-close')

        if (!fab || !panel) return

        fab.addEventListener('click', () => {
            const opening = panel.hidden
            panel.hidden = !opening
            if (opening) {
                if (this._activeTab === 'tips') {
                    this._loadFloatingTips()
                    this._renderFloatingTip()
                    this._startCountdownTimer()
                } else {
                    this._loadNotifications()
                }
            } else {
                this._stopCountdownTimer()
            }
        })

        closeBtn?.addEventListener('click', () => {
            panel.hidden = true
            this._stopCountdownTimer()
        })

        document.getElementById('mascot-tip-prev')?.addEventListener('click', () => this._stepFloating(-1))
        document.getElementById('mascot-tip-next')?.addEventListener('click', () => this._stepFloating(1))

        this._setupTabs()

        I18n.onChange(() => {
            if (!panel.hidden) {
                if (this._activeTab === 'tips') {
                    this._loadFloatingTips()
                    this._renderFloatingTip()
                } else {
                    this._loadNotifications()
                }
            }
        })

        this._startPushSchedule()
    }

    // ── Popup periódico: pop da próxima dica NEW (marcando-a como SHOWN) ─────────

    static _startPushSchedule() {
        if (this._pushTimer) return
        const storedNext = Number(localStorage.getItem(this._STORAGE_NEXT) ?? 0)
        if (storedNext > Date.now()) {
            this._nextTipTime = storedNext
            this._currentTip  = localStorage.getItem(this._STORAGE_TIP) ?? ''
            this._pushTimer   = setTimeout(() => this._popNextTip(), storedNext - Date.now())
        } else {
            this._pushTimer = setTimeout(() => this._popNextTip(), this._FIRST_POP_MS)
        }
    }

    static _popNextTip() {
        const raw  = this._getAgentTipsRaw()
        const next = (raw ?? []).find(t => t.status === 'NEW' && !this._poppedIds.has(t.id))

        let text
        if (next) {
            this._poppedIds.add(next.id)
            text = renderTipText(next)
            try { doRequest(`/api/finny/tips/${next.id}/shown`, 'POST') } catch { /* silencioso */ }
            next.status = 'SHOWN' // passa a aparecer no modal
        } else {
            text = this._pickStatic()
        }
        this._lastPopText = text

        this._currentTip  = text
        this._nextTipTime = Date.now() + this._TIP_PUSH_MS
        localStorage.setItem(this._STORAGE_TIP,  this._currentTip)
        localStorage.setItem(this._STORAGE_NEXT, String(this._nextTipTime))

        if (text) showToast(`💡 ${text}`, 'info', null, { saveToHistory: false })

        const panel = document.getElementById('mascot-panel')
        if (panel && !panel.hidden && this._activeTab === 'tips') {
            this._items = this._buildActiveItems()
            this._renderFloatingTip()
        }

        this._pushTimer = setTimeout(() => this._popNextTip(), this._TIP_PUSH_MS)
    }

    /** Escolhe uma dica estática evitando repetir a anterior. */
    static _pickStatic() {
        const pool = STATIC_TIPS[I18n.getLanguage()] ?? STATIC_TIPS.pt
        if (!pool.length) return ''
        if (pool.length === 1) return pool[0]
        let text = ''
        for (let i = 0; i < 5; i++) {
            text = pool[Math.floor(Math.random() * pool.length)]
            if (text !== this._lastPopText) break
        }
        return text
    }

    static _setupTabs() {
        document.getElementById('mascot-tab-tips')?.addEventListener('click', () => this._switchTab('tips'))
        document.getElementById('mascot-tab-messages')?.addEventListener('click', () => this._switchTab('messages'))
    }

    static _switchTab(tab) {
        this._activeTab = tab

        const tipsContent = document.getElementById('mascot-content-tips')
        const msgContent  = document.getElementById('mascot-content-messages')
        const tipBtn      = document.getElementById('mascot-tab-tips')
        const msgBtn      = document.getElementById('mascot-tab-messages')

        if (tipsContent) tipsContent.hidden = tab !== 'tips'
        if (msgContent)  msgContent.hidden  = tab !== 'messages'

        tipBtn?.classList.toggle('mascot-tab-btn--active', tab === 'tips')
        msgBtn?.classList.toggle('mascot-tab-btn--active', tab === 'messages')

        if (tab === 'tips') {
            this._loadFloatingTips()
            this._renderFloatingTip()
            this._startCountdownTimer()
        } else {
            this._stopCountdownTimer()
            this._loadNotifications()
        }
    }

    // ── Cache / itens ───────────────────────────────────────────────────────────

    static _getAgentTipsRaw(force = false) {
        const now = Date.now()
        if (!force && this._agentTipsRaw && (now - this._agentCacheTime) < this._AGENT_TTL) {
            return this._agentTipsRaw
        }
        this._agentTipsRaw = fetchAgentTips()
        this._agentCacheTime = now
        return this._agentTipsRaw
    }

    static _buildActiveItems() {
        return shownToItems(this._getAgentTipsRaw())
    }

    static _loadFloatingTips() {
        this._items = this._buildActiveItems()
        if (this._index >= this._items.length) this._index = 0
    }

    static _renderFloatingTip() {
        const tipEl     = document.getElementById('mascot-tip-text')
        const counterEl = document.getElementById('mascot-tip-counter')
        const barEl     = document.getElementById('mascot-tip-feedback')
        if (!tipEl) return

        const items = this._items ?? []

        if (!items.length) {
            tipEl.textContent = I18n.t('finnyNoActiveTips')
            if (counterEl) counterEl.textContent = ''
            this._wireFeedback(barEl, null)
            return
        }

        if (this._index >= items.length) this._index = 0
        const item = items[this._index]
        tipEl.textContent = item.text
        if (counterEl) counterEl.textContent = `${this._index + 1} / ${items.length}`

        this._wireFeedback(barEl, item, () => {
            this._items = this._buildActiveItems()
            if (this._index >= this._items.length) this._index = Math.max(0, this._items.length - 1)
            this._renderFloatingTip()
        })
    }

    static _stepFloating(dir) {
        if (!this._items?.length) return
        this._index = (this._index + dir + this._items.length) % this._items.length
        this._renderFloatingTip()
    }

    /** Liga/desliga a barra de feedback (👍 👎 ✕) para o item atual. */
    static _wireFeedback(barEl, item, onDone) {
        if (!barEl) return
        if (!item || !item.feedbackable || item.id == null) { barEl.hidden = true; return }
        barEl.hidden = false
        for (const btn of barEl.querySelectorAll('.finny-fb-btn')) {
            btn.onclick = () => { this._submitFeedback(item.id, btn.dataset.fb); onDone?.() }
        }
    }

    static _submitFeedback(id, feedback) {
        try { doRequest(`/api/finny/tips/${id}/feedback`, 'POST', { feedback }) } catch { /* silencioso */ }
        // Remove do cache local para a dica sair do modal (passa a viver só no histórico).
        if (Array.isArray(this._agentTipsRaw)) this._agentTipsRaw = this._agentTipsRaw.filter(t => t.id !== id)
        showToast(I18n.t('finnyFeedbackThanks'), 'success', null, { saveToHistory: false })
    }

    static _formatCountdown(ms) {
        if (ms <= 0) return '00:00'
        const totalSec = Math.floor(ms / 1000)
        const h  = Math.floor(totalSec / 3600)
        const m  = Math.floor((totalSec % 3600) / 60)
        const s  = totalSec % 60
        const mm = String(m).padStart(2, '0')
        const ss = String(s).padStart(2, '0')
        return h > 0 ? `${h}h ${mm}:${ss}` : `${mm}:${ss}`
    }

    static _startCountdownTimer() {
        this._stopCountdownTimer()
        const tick = () => {
            const el = document.getElementById('mascot-tip-countdown')
            if (!el) return
            const ms = Math.max(0, this._nextTipTime - Date.now())
            el.textContent = `${I18n.t('tipCountdownLabel')} ${this._formatCountdown(ms)}`
        }
        tick()
        this._countdownTimer = setInterval(tick, 1000)
    }

    static _stopCountdownTimer() {
        if (this._countdownTimer) { clearInterval(this._countdownTimer); this._countdownTimer = null }
    }

    // ── Aba Mensagens (somente leitura: sem "marcar como lida" nem contagem) ─────

    static _loadNotifications(listId = 'mascot-notifications-list') {
        const list = document.getElementById(listId)
        if (!list) return

        const allNotifs = (doRequest('/api/notifications', 'GET') ?? []).slice(0, 10)
        list.innerHTML = ''

        if (allNotifs.length === 0) {
            const empty = _clone('tpl-mascot-empty')
            empty.textContent = I18n.t('mascotNoMessages')
            list.appendChild(empty)
            return
        }

        for (const item of allNotifs) {
            list.appendChild(item.type === 'USER_ACTION' ? this._buildLocalCard(item) : this._buildNotifCard(item))
        }
    }

    static _buildNotifCard(n) {
        const meta = TYPE_META[n.type] ?? { icon: '🔔', i18nKey: 'notifications' }
        const card = _clone('tpl-mascot-notif-card')

        card.querySelector('.mascot-notif-icon').textContent = meta.icon
        card.querySelector('.mn-label').textContent = I18n.t(meta.i18nKey, meta.i18nParams) + ': '
        card.querySelector('.mn-name').textContent = n.goalName ?? ''
        card.querySelector('.mascot-notif-date').textContent = _notifDate(n.createdAt)

        const viewBtn = card.querySelector('.notif-view-btn')
        if (n.link) { viewBtn.textContent = I18n.t('commonView'); viewBtn.addEventListener('click', () => navigate(n.link)) }
        else card.querySelector('.mascot-notif-actions').remove()

        return card
    }

    static _buildLocalCard(n) {
        const TYPE_COLOR = { success: '#22c55e', error: '#ef4444', warning: '#f59e0b', info: '#3b82f6' }
        const card = _clone('tpl-mascot-local-card')
        card.style.setProperty('--local-card-color', TYPE_COLOR[n.severity] ?? TYPE_COLOR.info)
        card.querySelector('.mascot-notif-title').textContent = n.message
        card.querySelector('.mascot-notif-date').textContent = _notifDate(n.createdAt)

        const viewBtn = card.querySelector('.local-view-btn')
        if (n.link) { viewBtn.textContent = I18n.t('commonView'); viewBtn.addEventListener('click', () => navigate(n.link)) }
        else card.querySelector('.mascot-notif-actions').remove()

        return card
    }

    // ── Widget do dashboard ─────────────────────────────────────────────────────

    /** Re-renderiza o painel flutuante (chamado quando o dashboard recarrega). */
    static refreshFloatingTips() {
        const panel = document.getElementById('mascot-panel')
        if (panel && !panel.hidden && this._activeTab === 'tips') {
            this._items = this._buildActiveItems()
            this._renderFloatingTip()
        }
    }

    static renderDashboardWidget() {
        this._dashItems = this._buildDashItems()
        this._dashIndex = 0
        this._renderDashboardTip()

        const prevBtn = document.getElementById('dashboard-mascot-prev')
        const nextBtn = document.getElementById('dashboard-mascot-next')
        if (prevBtn) prevBtn.onclick = () => {
            if (!this._dashItems.length) return
            this._dashIndex = (this._dashIndex - 1 + this._dashItems.length) % this._dashItems.length
            this._renderDashboardTip()
        }
        if (nextBtn) nextBtn.onclick = () => {
            if (!this._dashItems.length) return
            this._dashIndex = (this._dashIndex + 1) % this._dashItems.length
            this._renderDashboardTip()
        }
    }

    /** Widget mostra dicas SHOWN; se não houver nenhuma, cai numa dica estática (sem feedback). */
    static _buildDashItems() {
        const active = this._buildActiveItems()
        if (active.length) return active
        const text = randomStatic(I18n.getLanguage())
        return text ? [{ id: null, text, severity: 'info', feedbackable: false }] : []
    }

    static _renderDashboardTip() {
        const tipEl     = document.getElementById('dashboard-mascot-tip')
        const counterEl = document.getElementById('dashboard-mascot-counter')
        const barEl     = document.getElementById('dashboard-mascot-feedback')
        if (!tipEl || !this._dashItems.length) return

        if (this._dashIndex >= this._dashItems.length) this._dashIndex = 0
        const item = this._dashItems[this._dashIndex]
        tipEl.textContent = item.text
        if (counterEl) counterEl.textContent = `${this._dashIndex + 1} / ${this._dashItems.length}`

        this._wireFeedback(barEl, item, () => {
            this._dashItems = this._buildDashItems()
            if (this._dashIndex >= this._dashItems.length) this._dashIndex = Math.max(0, this._dashItems.length - 1)
            this._renderDashboardTip()
        })
    }
}
