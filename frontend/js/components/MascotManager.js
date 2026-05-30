import { doRequest, navigate, showToast } from '../../utils/FrontendFunctions.js'
import { SidebarManager } from './SidebarManager.js'
import { I18n } from '../i18n.js'

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


function buildSavingsTip(totalIncome, net, lang) {
    if (totalIncome <= 0 || net <= 0) return null
    const pct = Math.round((net / totalIncome) * 100)
    if (pct < 10) return ({
        pt: `Você está economizando apenas ${pct}% da sua renda. Tente aumentar gradualmente para pelo menos 20%!`,
        en: `You're saving only ${pct}% of your income. Try to gradually increase to at least 20%!`,
        es: `Estás ahorrando solo el ${pct}% de tus ingresos. ¡Intenta aumentar gradualmente al menos al 20%!`,
    }[lang] ?? '')
    if (pct < 20) return ({
        pt: `Você está economizando ${pct}% da sua renda. Bom progresso! Tente chegar a 20% para maior segurança.`,
        en: `You're saving ${pct}% of your income. Good progress! Try to reach 20% for greater security.`,
        es: `Estás ahorrando el ${pct}% de tus ingresos. ¡Buen progreso! Intenta llegar al 20% para mayor seguridad.`,
    }[lang] ?? '')
    return ({
        pt: `Ótimo trabalho! Você está economizando ${pct}% da sua renda no período. Continue assim!`,
        en: `Great job! You're saving ${pct}% of your income this period. Keep it up!`,
        es: `¡Excelente trabajo! Estás ahorrando ${pct}% de tus ingresos en este período. ¡Sigue así!`,
    }[lang] ?? '')
}

function buildBalanceTip(balances, lang) {
    if (!balances || balances.length < 2) return null
    const last = balances[balances.length - 1]?.balance ?? 0
    const prev = balances[balances.length - 2]?.balance ?? 0
    if (prev <= 0 || last >= prev) return null
    const dropPct = Math.round(((prev - last) / prev) * 100)
    if (dropPct < 5) return null
    return ({
        pt: `Seu patrimônio caiu ${dropPct}% no último período. Que tal revisar seus gastos?`,
        en: `Your net worth dropped ${dropPct}% in the last period. How about reviewing your expenses?`,
        es: `Tu patrimonio cayó un ${dropPct}% en el último período. ¿Qué tal revisar tus gastos?`,
    }[lang] ?? '')
}

function buildSingleGoalTip(type, pct, name, lang) {
    if (type === 'EXPENSE_LIMIT') {
        if (pct >= 90) return ({
            pt: `Atenção! Você atingiu ${Math.round(pct)}% do limite de gastos em "${name}". Cuidado com novos gastos!`,
            en: `Watch out! You've used ${Math.round(pct)}% of your spending limit for "${name}". Be careful with new expenses!`,
            es: `¡Atención! Has utilizado el ${Math.round(pct)}% de tu límite de gastos en "${name}". ¡Cuidado con nuevos gastos!`,
        }[lang] ?? '')
        if (pct >= 70) return ({
            pt: `Você está em ${Math.round(pct)}% do limite de gastos em "${name}". Fique atento!`,
            en: `You're at ${Math.round(pct)}% of your spending limit for "${name}". Keep an eye on it!`,
            es: `Estás al ${Math.round(pct)}% de tu límite de gastos en "${name}". ¡Presta atención!`,
        }[lang] ?? '')
    } else {
        if (pct >= 75) return ({
            pt: `Você está quase lá! ${Math.round(pct)}% da meta "${name}" conquistada. Continue!`,
            en: `Almost there! You've reached ${Math.round(pct)}% of your "${name}" goal. Keep going!`,
            es: `¡Casi llegas! Has alcanzado el ${Math.round(pct)}% de tu meta "${name}". ¡Sigue así!`,
        }[lang] ?? '')
        if (pct >= 50) return ({
            pt: `Metade do caminho! Você atingiu ${Math.round(pct)}% da meta "${name}".`,
            en: `Halfway there! You've reached ${Math.round(pct)}% of your "${name}" goal.`,
            es: `¡A mitad de camino! Has alcanzado el ${Math.round(pct)}% de tu meta "${name}".`,
        }[lang] ?? '')
    }
    return null
}

function buildDeadlineTip(endDate, name, today, lang) {
    const daysLeft = Math.ceil((new Date(endDate) - today) / (1000 * 60 * 60 * 24))
    if (daysLeft <= 0 || daysLeft > 7) return null
    return ({
        pt: `Sua meta "${name}" vence em ${daysLeft} dia${daysLeft === 1 ? '' : 's'}. Não desista!`,
        en: `Your goal "${name}" expires in ${daysLeft} day${daysLeft === 1 ? '' : 's'}. Don't give up!`,
        es: `Tu meta "${name}" vence en ${daysLeft} día${daysLeft === 1 ? '' : 's'}. ¡No te rindas!`,
    }[lang] ?? '')
}

function buildGoalTips(goals, lang) {
    const tips  = []
    const today = new Date()
    let count   = 0
    for (const goal of goals) {
        if (goal.status !== 'ACTIVE' || count >= 3) continue
        const pct  = goal.progressPercent ?? 0
        const name = goal.name ?? ''
        const tip  = buildSingleGoalTip(goal.type, pct, name, lang)
        if (tip) { tips.push(tip); count++ }
        if (goal.endDate && count < 3) {
            const deadline = buildDeadlineTip(goal.endDate, name, today, lang)
            if (deadline) { tips.push(deadline); count++ }
        }
    }
    return tips
}

function buildPersonalizedTips(data, lang, goals = []) {
    if (!data) return []

    const totalIncome   = data.monthlyData?.reduce((s, m) => s + (m.income   ?? 0), 0) ?? 0
    const totalExpenses = data.monthlyData?.reduce((s, m) => s + (m.expenses ?? 0), 0) ?? 0
    const net           = totalIncome - totalExpenses
    const tips          = []

    if (totalIncome === 0 && totalExpenses === 0) {
        tips.push({
            pt: 'Registre suas receitas e despesas para receber dicas personalizadas ao seu perfil financeiro!',
            en: 'Log your income and expenses to get tips tailored to your financial profile!',
            es: '¡Registra tus ingresos y gastos para recibir consejos personalizados a tu perfil financiero!',
        }[lang] ?? '')
    }

    if (totalIncome > 0 && totalExpenses > totalIncome) {
        const pct = Math.round((totalExpenses / totalIncome - 1) * 100)
        tips.push({
            pt: `Atenção! Seus gastos estão ${pct}% acima da sua renda no período. Que tal revisar onde dá para economizar?`,
            en: `Watch out! Your expenses are ${pct}% above your income this period. How about reviewing where you can cut back?`,
            es: `¡Atención! Tus gastos son ${pct}% superiores a tus ingresos en este período. ¿Qué tal revisar dónde puedes ahorrar?`,
        }[lang] ?? '')
    }

    const savingsTip = buildSavingsTip(totalIncome, net, lang)
    if (savingsTip) tips.push(savingsTip)

    const topCategory = data.categoryExpenses?.[0]
    if (topCategory?.categoryName) {
        tips.push({
            pt: `Sua maior categoria de gastos é "${topCategory.categoryName}". Vale analisar se há como reduzir!`,
            en: `Your biggest spending category is "${topCategory.categoryName}". Worth analyzing if there's room to cut back!`,
            es: `Tu mayor categoría de gastos es "${topCategory.categoryName}". ¡Vale la pena analizar si hay forma de reducirlos!`,
        }[lang] ?? '')
    }

    const balanceTip = buildBalanceTip(data.balanceEvolution, lang)
    if (balanceTip) tips.push(balanceTip)

    tips.push(...buildGoalTips(goals, lang))

    return tips.filter(Boolean)
}


function toDateStr(d) {
    return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().split('T')[0]
}

function currentMonthRange() {
    const today = new Date()
    const first = new Date(today.getFullYear(), today.getMonth(), 1)
    return { startDate: toDateStr(first), endDate: toDateStr(today) }
}

function escapeHtml(str) {
    return String(str ?? '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;')
}

function fetchGoals() {
    try { return doRequest('/api/goals', 'GET') ?? [] } catch { return [] }
}


export class MascotManager {
    static _tips           = []
    static _currentTip     = ''
    static _nextTipTime    = 0
    static _countdownTimer = null
    static _dashTips       = []
    static _dashIndex      = 0
    static _activeTab      = 'tips'
    static _pushTimer      = null
    static _TIP_PUSH_MS    = 30 * 60 * 1000
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

        this.refreshBadge()
        this._startPushSchedule()
    }

    static _startPushSchedule() {
        if (this._pushTimer) return

        const pickRandom = () => {
            const lang = I18n.getLanguage()
            const pool = [...(STATIC_TIPS[lang] ?? STATIC_TIPS.pt), ...this._tips]
            return pool.length ? pool[Math.floor(Math.random() * pool.length)] : ''
        }

        const rotateTip = () => {
            this._currentTip  = pickRandom()
            this._nextTipTime = Date.now() + this._TIP_PUSH_MS
            localStorage.setItem(this._STORAGE_TIP,  this._currentTip)
            localStorage.setItem(this._STORAGE_NEXT, String(this._nextTipTime))
            if (this._currentTip) showToast(`💡 ${this._currentTip}`, 'info', null, { saveToHistory: false })
            const panel = document.getElementById('mascot-panel')
            if (panel && !panel.hidden && this._activeTab === 'tips') this._renderFloatingTip()
            this._pushTimer = setTimeout(rotateTip, this._TIP_PUSH_MS)
        }

        const storedTip  = localStorage.getItem(this._STORAGE_TIP)
        const storedNext = Number(localStorage.getItem(this._STORAGE_NEXT) ?? 0)

        if (storedTip && storedNext > Date.now()) {
            this._currentTip  = storedTip
            this._nextTipTime = storedNext
            this._pushTimer   = setTimeout(rotateTip, storedNext - Date.now())
        } else {
            this._currentTip  = pickRandom()
            this._nextTipTime = Date.now() + this._TIP_PUSH_MS
            localStorage.setItem(this._STORAGE_TIP,  this._currentTip)
            localStorage.setItem(this._STORAGE_NEXT, String(this._nextTipTime))
            setTimeout(() => {
                if (this._currentTip) showToast(`💡 ${this._currentTip}`, 'info', null, { saveToHistory: false })
            }, 5000)
            this._pushTimer = setTimeout(rotateTip, this._TIP_PUSH_MS)
        }
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
            if (!this._tips.length) this._loadFloatingTips()
            this._renderFloatingTip()
            this._startCountdownTimer()
        } else {
            this._stopCountdownTimer()
            this._loadNotifications()
        }
    }

    static _loadFloatingTips() {
        const lang       = I18n.getLanguage()
        const staticTips = [...(STATIC_TIPS[lang] ?? STATIC_TIPS.pt)]
        try {
            const { startDate, endDate } = currentMonthRange()
            const data  = doRequest(`/api/reports/dashboard?startDate=${startDate}&endDate=${endDate}`, 'GET')
            const goals = fetchGoals()
            const personalized = buildPersonalizedTips(data, lang, goals)
            this._tips = [...personalized, ...staticTips]
        } catch {
            this._tips = staticTips
        }
    }

    static _renderFloatingTip() {
        const tipEl = document.getElementById('mascot-tip-text')
        if (tipEl) tipEl.textContent = this._currentTip || ''
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

    static _loadNotifications(listId = 'mascot-notifications-list', footerId = 'mascot-notif-footer', markAllId = 'mascot-mark-all-read') {
        const list   = document.getElementById(listId)
        const footer = document.getElementById(footerId)
        if (!list) return

        list.innerHTML = `<div class="mascot-notif-empty mascot-notif-loading">…</div>`

        const allNotifs = doRequest('/api/notifications', 'GET') ?? []

        list.innerHTML = ''

        if (allNotifs.length === 0) {
            list.innerHTML = `<div class="mascot-notif-empty">${I18n.t('mascotNoMessages')}</div>`
            if (footer) footer.hidden = true
            return
        }

        if (footer) footer.hidden = false

        for (const item of allNotifs) {
            list.appendChild(item.type === 'USER_ACTION' ? this._buildLocalCard(item) : this._buildNotifCard(item))
        }

        const markAllBtn = document.getElementById(markAllId)
        if (markAllBtn) markAllBtn.onclick = () => {
            doRequest('/api/notifications/read-all', 'PUT')
            this._loadNotifications(listId, footerId, markAllId)
            this.refreshBadge()
        }
    }

    static _buildNotifCard(n) {
        const meta    = TYPE_META[n.type] ?? { icon: '🔔', i18nKey: 'notifications' }
        const label   = I18n.t(meta.i18nKey, meta.i18nParams)
        const dateStr = new Date(n.createdAt).toLocaleString(I18n.getLanguage(), { dateStyle: 'short', timeStyle: 'short' })

        const card = document.createElement('div')
        card.className = `mascot-notif-card${n.read ? ' mascot-notif-card--read' : ''}`
        card.innerHTML = `
            <span class="mascot-notif-icon">${meta.icon}</span>
            <div class="mascot-notif-body">
                <p class="mascot-notif-title">${label}: <strong>${escapeHtml(n.goalName ?? '')}</strong></p>
                <p class="mascot-notif-date">${dateStr}</p>
            </div>
            <div class="mascot-notif-actions">
                ${n.link  ? `<button class="btn btn-ghost btn-sm notif-view-btn">${I18n.t('commonView')}</button>` : ''}
                ${n.read  ? '' : `<button class="btn btn-ghost btn-sm notif-read-btn">${I18n.t('markAsRead')}</button>`}
            </div>
        `

        card.querySelector('.notif-view-btn')?.addEventListener('click', () => {
            this._markRead(n.id, card)
            navigate(n.link)
        })
        card.querySelector('.notif-read-btn')?.addEventListener('click', () => {
            this._markRead(n.id, card)
        })

        return card
    }

    static _buildLocalCard(n) {
        const TYPE_COLOR = { success: '#22c55e', error: '#ef4444', warning: '#f59e0b', info: '#3b82f6' }
        const color   = TYPE_COLOR[n.severity] ?? TYPE_COLOR.info
        const dateStr = new Date(n.createdAt).toLocaleString(I18n.getLanguage(), { dateStyle: 'short', timeStyle: 'short' })

        const card = document.createElement('div')
        card.className = 'mascot-notif-card mascot-notif-card--read mascot-notif-card--local'
        card.style.setProperty('--local-card-color', color)
        card.innerHTML = `
            <span class="mascot-notif-local-bar"></span>
            <div class="mascot-notif-body">
                <p class="mascot-notif-title">${escapeHtml(n.message)}</p>
                <p class="mascot-notif-date">${dateStr}</p>
            </div>
            ${n.link ? `<div class="mascot-notif-actions"><button class="btn btn-ghost btn-sm local-view-btn">${I18n.t('commonView')}</button></div>` : ''}
        `
        card.querySelector('.local-view-btn')?.addEventListener('click', () => navigate(n.link))
        return card
    }

    static _markRead(id, card) {
        doRequest(`/api/notifications/${id}/read`, 'PUT')
        card.classList.add('mascot-notif-card--read')
        card.querySelector('.notif-read-btn')?.remove()
        this.refreshBadge()
        SidebarManager.refreshNotificationBadge()
    }

    static refreshBadge() {
        try {
            const raw    = doRequest('/api/notifications/unread-count', 'GET')
            const count  = Math.max(0, Number(raw) || 0)
            const label  = count > 99 ? '99+' : String(count)

            const fabBadge = document.getElementById('mascot-fab-badge')
            const tabBadge = document.getElementById('mascot-tab-badge')

            if (fabBadge) { fabBadge.textContent = label; fabBadge.hidden = count === 0 }
            if (tabBadge) { tabBadge.textContent = label; tabBadge.hidden = false }
        } catch { /* silencioso — usuário pode não estar autenticado */ }
    }

    static refreshFloatingTips(data, goals = []) {
        const lang = I18n.getLanguage()
        const personalized = buildPersonalizedTips(data, lang, goals)
        this._tips = [...personalized, ...(STATIC_TIPS[lang] ?? STATIC_TIPS.pt)]
        const panel = document.getElementById('mascot-panel')
        if (panel && !panel.hidden && this._activeTab === 'tips') this._renderFloatingTip()
    }

    static _getStaticTips(lang) {
        return [...(STATIC_TIPS[lang] ?? STATIC_TIPS.pt)]
    }

    static _buildPersonalized(data, lang, goals = []) {
        return buildPersonalizedTips(data, lang, goals)
    }

    static renderDashboardWidget(data, goals = []) {
        const lang = I18n.getLanguage()
        const personalized = buildPersonalizedTips(data, lang, goals)
        this._dashTips  = [...personalized, ...(STATIC_TIPS[lang] ?? STATIC_TIPS.pt)]
        this._dashIndex = 0
        this._renderDashboardTip()

        const prevBtn = document.getElementById('dashboard-mascot-prev')
        const nextBtn = document.getElementById('dashboard-mascot-next')
        if (prevBtn) prevBtn.onclick = () => {
            this._dashIndex = (this._dashIndex - 1 + this._dashTips.length) % this._dashTips.length
            this._renderDashboardTip()
        }
        if (nextBtn) nextBtn.onclick = () => {
            this._dashIndex = (this._dashIndex + 1) % this._dashTips.length
            this._renderDashboardTip()
        }
    }

    static _renderDashboardTip() {
        const tipEl     = document.getElementById('dashboard-mascot-tip')
        const counterEl = document.getElementById('dashboard-mascot-counter')
        if (!tipEl || !this._dashTips.length) return
        tipEl.textContent = this._dashTips[this._dashIndex]
        if (counterEl) counterEl.textContent = `${this._dashIndex + 1} / ${this._dashTips.length}`
    }
}
