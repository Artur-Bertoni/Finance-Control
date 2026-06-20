import { doRequest, navigate, formatDateTime, initFilterToggle } from '../utils/FrontendFunctions.js'
import { I18n } from './i18n.js'
import { FinnySvg } from './utils/FinnySvg.js'
import { CustomSelect } from './components/CustomSelect.js'
import { SidebarManager } from './components/SidebarManager.js'

const FLATPICKR_LOCALES = { pt: 'pt', es: 'es' }
const NOTIF_PAGE_SIZE   = 10
const HISTORY_PAGE_SIZE = 10
let _allNotifs        = []
let _notifPage        = 0
let _allHistory       = []
let _historyPage      = 0
let _notifFilterToggle = null
let _notifFilterReady = false
let _fpStart = null
let _fpEnd   = null
let _i18nHooked = false
let _activeTab = 'tips'

const TYPE_META = {
    GOAL_MILESTONE_50:     { icon: '📊', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 50 } },
    GOAL_MILESTONE_75:     { icon: '📈', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 75 } },
    GOAL_MILESTONE_90:     { icon: '🔥', i18nKey: 'notifGoalMilestone', i18nParams: { percent: 90 } },
    GOAL_COMPLETED:        { icon: '🎯', i18nKey: 'notifGoalCompleted'   },
    GOAL_EXCEEDED:         { icon: '⚠️', i18nKey: 'notifGoalExceeded'   },
    GOAL_DEADLINE_WARNING: { icon: '⏰', i18nKey: 'notifGoalDeadline'   },
}

const TYPE_COLOR     = { success: '#22c55e', error: '#ef4444', warning: '#f59e0b', info: '#3b82f6' }
const SEVERITY_COLOR = { success: '#22c55e', warning: '#f59e0b', info: '#3b82f6' }
const STATUS_TO_FB = { HELPFUL: 'HELPFUL', NOT_HELPFUL: 'NOT_HELPFUL', DISMISSED: 'DISMISSED' }
const FB_TITLE     = { HELPFUL: 'finnyFeedbackHelpful', NOT_HELPFUL: 'finnyFeedbackNotHelpful', DISMISSED: 'finnyFeedbackDismiss' }

function _clone(id) {
    return document.getElementById(id).content.firstElementChild.cloneNode(true)
}

function _emptyState(container, msgKey) {
    container.innerHTML = ''
    const el = _clone('tpl-finny-empty')
    el.textContent = I18n.t(msgKey)
    container.appendChild(el)
}

function _notifDate(iso) {
    return new Date(iso).toLocaleString(I18n.getLanguage(), { dateStyle: 'short', timeStyle: 'short' })
}

export function init() {
    FinnySvg.autoInit()
    document.body.classList.add('page-finny')
    if (!_i18nHooked) { _i18nHooked = true; I18n.onChange(_onLangChange) }
    _notifFilterReady = false
    _notifPage = 0
    _fpStart = null
    _fpEnd = null
    _setupTabs()
    _switchTab('tips')
}

function _setupTabs() {
    document.getElementById('nc-tab-tips')?.addEventListener('click',     () => _switchTab('tips'))
    document.getElementById('nc-tab-history')?.addEventListener('click',  () => _switchTab('history'))
    document.getElementById('nc-tab-messages')?.addEventListener('click', () => _switchTab('messages'))
}

function _switchTab(tab) {
    _activeTab = tab
    const map = {
        tips:     document.getElementById('nc-content-tips'),
        history:  document.getElementById('nc-content-history'),
        messages: document.getElementById('nc-content-messages'),
    }
    const btns = {
        tips:     document.getElementById('nc-tab-tips'),
        history:  document.getElementById('nc-tab-history'),
        messages: document.getElementById('nc-tab-messages'),
    }
    for (const [key, el] of Object.entries(map))  if (el) el.hidden = key !== tab
    for (const [key, el] of Object.entries(btns)) el?.classList.toggle('finny-page-tab--active', key === tab)

    if (tab === 'tips')     _loadActiveTips()
    if (tab === 'history')  _loadHistory()
    if (tab === 'messages') _loadMessages()
}

function _loadActiveTips() {
    const list = document.getElementById('nc-tips-list')
    if (!list) return

    const tips = (doRequest('/api/finny/tips', 'GET') ?? []).filter(t => t.status === 'SHOWN')

    if (tips.length === 0) {
        _emptyState(list, 'finnyNoActiveTips')
        return
    }

    list.innerHTML = ''
    for (const tip of tips) list.appendChild(_buildActiveTipCard(tip))
}

function _buildActiveTipCard(tip) {
    const card = _tipCardShell(tip)
    const bar  = _feedbackBar(tip, null, (fb) => {
        doRequest(`/api/finny/tips/${tip.id}/feedback`, 'POST', { feedback: fb })
        card.remove()
        if (!document.querySelector('#nc-tips-list .finny-history-card')) _loadActiveTips()
    })
    card.querySelector('.finny-history-body').appendChild(bar)
    return card
}

function _loadHistory() {
    _renderStats()
    _allHistory = doRequest('/api/finny/tips/history', 'GET') ?? []
    _historyPage = 0
    _renderHistory()
}

function _renderStats() {
    const grid = document.getElementById('nc-stats-grid')
    if (!grid) return
    const stats = doRequest('/api/finny/tips/stats', 'GET')
    if (!stats) { grid.innerHTML = ''; return }

    const cards = [
        { label: I18n.t('finnyStatsTotalTips'),     value: String(stats.totalTips ?? 0) },
        { label: I18n.t('finnyStatsHelpful'),       value: String(stats.helpfulCount ?? 0) },
        { label: I18n.t('finnyStatsSavingsRate'),   value: `${Math.round(stats.savingsRatePct ?? 0)}%` },
        { label: I18n.t('finnyStatsEmergencyFund'), value: `${(stats.emergencyFundMonths ?? 0).toFixed(1)} ${I18n.t('finnyStatsMonthsUnit')}` },
    ]
    grid.innerHTML = ''
    for (const c of cards) {
        const el = _clone('tpl-finny-stat-card')
        el.querySelector('.finny-stat-value').textContent = c.value
        el.querySelector('.finny-stat-label').textContent = c.label
        grid.appendChild(el)
    }
}

function _renderHistory() {
    const list = document.getElementById('nc-history-list')
    if (!list) return

    const total      = _allHistory.length
    const totalPages = Math.max(1, Math.ceil(total / HISTORY_PAGE_SIZE))
    if (_historyPage >= totalPages) _historyPage = totalPages - 1
    const pageItems  = _allHistory.slice(_historyPage * HISTORY_PAGE_SIZE, (_historyPage + 1) * HISTORY_PAGE_SIZE)

    list.innerHTML = ''
    if (total === 0) {
        _emptyState(list, 'finnyHistoryEmpty')
        _renderPager(document.getElementById('nc-history-pagination'), 0, HISTORY_PAGE_SIZE, 0, () => {})
        return
    }

    for (const tip of pageItems) list.appendChild(_buildHistoryCard(tip))
    _renderPager(document.getElementById('nc-history-pagination'), _historyPage, HISTORY_PAGE_SIZE, total,
        p => { _historyPage = p; _renderHistory() })
}

function _buildHistoryCard(tip) {
    const card    = _tipCardShell(tip, true)
    const current = STATUS_TO_FB[tip.status] ?? null
    const bar     = _feedbackBar(tip, current, (fb, barEl) => {
        doRequest(`/api/finny/tips/${tip.id}/feedback`, 'POST', { feedback: fb })
        tip.status = fb
        _highlightFeedback(barEl, fb)
    })
    card.querySelector('.finny-history-body').appendChild(bar)
    return card
}

function _tipCardShell(tip, withDate = false) {
    const card = _clone('tpl-finny-tip-card')
    card.style.setProperty('--finny-card-color', SEVERITY_COLOR[tip.severity] ?? SEVERITY_COLOR.info)
    card.querySelector('.finny-history-text').textContent = I18n.t('finnyTip_' + tip.ruleKey, tip.params ?? {})
    card.querySelector('.finny-history-chip').textContent = I18n.t('finnyCategory_' + tip.category)

    const dateEl = card.querySelector('.finny-history-date')
    if (withDate) dateEl.textContent = formatDateTime(tip.createdAt)
    else dateEl.remove()

    return card
}

function _feedbackBar(tip, currentFb, onPick) {
    const bar = _clone('tpl-finny-feedback-bar')
    for (const btn of bar.querySelectorAll('.finny-fb-btn')) {
        const fb = btn.dataset.fb
        btn.title = I18n.t(FB_TITLE[fb])
        btn.classList.toggle('finny-fb-btn--active', currentFb === fb)
        btn.addEventListener('click', () => onPick(fb, bar))
    }
    return bar
}

function _highlightFeedback(barEl, fb) {
    for (const btn of barEl.querySelectorAll('.finny-fb-btn')) {
        btn.classList.toggle('finny-fb-btn--active', btn.dataset.fb === fb)
    }
}

function _loadMessages() {
    _allNotifs = doRequest('/api/notifications', 'GET') ?? []
    _setupNotifFilter()
    _notifPage = 0
    _renderMessages()
}

function _setupNotifFilter() {
    if (_notifFilterReady) return
    _notifFilterReady = true

    _notifFilterToggle = initFilterToggle(_isNotifFilterActive)

    const onChange = () => {
        _notifPage = 0
        _notifFilterToggle?.syncActive()
        _syncNotifClear()
        _renderMessages()
    }

    _fpStart = _initDateTimePicker('nc-date-start', onChange)
    _fpEnd   = _initDateTimePicker('nc-date-end', onChange)

    document.getElementById('nc-clear-filter-btn')?.addEventListener('click', () => {
        _fpStart?.clear()
        _fpEnd?.clear()
        onChange()
    })
    _syncNotifClear()
}

function _initDateTimePicker(inputId, onChange) {
    const input = document.getElementById(inputId)
    if (!input || typeof flatpickr === 'undefined') return null

    const lang      = I18n.getLanguage()
    const locale    = flatpickr.l10ns?.[FLATPICKR_LOCALES[lang]] ?? undefined
    const altFormat = (lang === 'en' ? 'm/d/Y' : 'd/m/Y') + ' H:i'

    const fp = flatpickr(input, {
        enableTime:    true,
        time_24hr:     true,
        dateFormat:    'Y-m-d H:i',
        altInput:      true,
        altFormat,
        altInputClass: 'flatpickr-input fc-date-input',
        maxDate:       'today',
        disableMobile: true,
        allowInput:    false,
        ...(locale ? { locale } : {}),
        onChange:      () => onChange(),
    })

    if (fp.altInput) {
        fp.altInput.dataset.i18nPlaceholder = 'finnyFilterPlaceholder'
        fp.altInput.placeholder = I18n.t('finnyFilterPlaceholder')
    }

    SidebarManager._attachWheelMonthNav(fp)
    return fp
}

function _onLangChange() {
    _applyPickerLocale()
    if (_activeTab === 'tips')     _loadActiveTips()
    if (_activeTab === 'history')  _loadHistory()
    if (_activeTab === 'messages') _renderMessages()
}

function _applyPickerLocale() {
    if (typeof flatpickr === 'undefined') return
    const lang      = I18n.getLanguage()
    const locale    = flatpickr.l10ns?.[FLATPICKR_LOCALES[lang]] ?? flatpickr.l10ns.default
    const altFormat = (lang === 'en' ? 'm/d/Y' : 'd/m/Y') + ' H:i'
    for (const fp of [_fpStart, _fpEnd]) {
        if (!fp?.altInput?.isConnected) continue
        fp.set('locale', locale)
        fp.set('altFormat', altFormat)
    }
}

function _isNotifFilterActive() {
    return !!(_fpStart?.selectedDates?.length || _fpEnd?.selectedDates?.length)
}

function _syncNotifClear() {
    const wrapper = document.getElementById('nc-clear-filter-btn')?.closest('.filter-clear-field')
    if (wrapper) wrapper.style.display = _isNotifFilterActive() ? '' : 'none'
}

function _createdMs(n) {
    const s = n.createdAt
    if (!s) return 0
    const hasZone = s.endsWith('Z') || /[+-]\d{2}:\d{2}$/.test(s)
    return new Date(hasZone ? s : s + 'Z').getTime()
}

function _filteredNotifs() {
    const startMs = _fpStart?.selectedDates?.[0] ? _fpStart.selectedDates[0].getTime() : -Infinity
    const endMs   = _fpEnd?.selectedDates?.[0]   ? _fpEnd.selectedDates[0].getTime()   :  Infinity
    return _allNotifs.filter(n => { const t = _createdMs(n); return t >= startMs && t <= endMs })
}

function _renderMessages() {
    const list = document.getElementById('nc-notifications-list')
    if (!list) return

    const filtered   = _filteredNotifs()
    const totalPages = Math.max(1, Math.ceil(filtered.length / NOTIF_PAGE_SIZE))
    if (_notifPage >= totalPages) _notifPage = totalPages - 1
    const pageItems  = filtered.slice(_notifPage * NOTIF_PAGE_SIZE, (_notifPage + 1) * NOTIF_PAGE_SIZE)

    list.innerHTML = ''
    if (filtered.length === 0) {
        _emptyState(list, 'mascotNoMessages')
        _renderPager(document.getElementById('nc-notifications-pagination'), 0, NOTIF_PAGE_SIZE, 0, () => {})
        return
    }

    for (const item of pageItems) {
        list.appendChild(item.type === 'USER_ACTION' ? _buildLocalCard(item) : _buildBackendCard(item))
    }
    _renderPager(document.getElementById('nc-notifications-pagination'), _notifPage, NOTIF_PAGE_SIZE, filtered.length,
        p => { _notifPage = p; _renderMessages() })
}

function _renderPager(container, page, pageSize, totalItems, onGoto) {
    if (!container) return
    const totalPages = Math.max(1, Math.ceil(totalItems / pageSize))
    container.innerHTML = ''
    container.hidden = totalPages <= 1
    if (totalPages <= 1) return

    container.appendChild(document.getElementById('tpl-pagination').content.cloneNode(true))
    const prevBtn = container.querySelector('.pg-prev')
    const nextBtn = container.querySelector('.pg-next')
    const select  = container.querySelector('.pagination-select')
    const info    = container.querySelector('.pagination-info')

    prevBtn.disabled = page === 0
    prevBtn.addEventListener('click', () => onGoto(page - 1))
    nextBtn.disabled = page === totalPages - 1
    nextBtn.addEventListener('click', () => onGoto(page + 1))

    for (let i = 1; i <= totalPages; i++) {
        const opt = document.createElement('option')
        opt.value = i
        opt.textContent = `${i}/${totalPages}`
        opt.selected = i === page + 1
        select.appendChild(opt)
    }
    select.addEventListener('change', () => onGoto(Number(select.value) - 1))
    CustomSelect.wrap(select)

    info.textContent = I18n.t('commonPaginationInfo', {
        start: page * pageSize + 1,
        end:   Math.min((page + 1) * pageSize, totalItems),
        total: totalItems,
    })
}

function _buildBackendCard(n) {
    const meta = TYPE_META[n.type] ?? { icon: '🔔', i18nKey: 'notifications' }
    const card = _clone('tpl-finny-notif-card')

    card.querySelector('.mascot-notif-icon').textContent = meta.icon
    card.querySelector('.nc-notif-label').textContent = I18n.t(meta.i18nKey, meta.i18nParams) + ': '
    card.querySelector('.nc-notif-name').textContent = n.goalName ?? ''
    card.querySelector('.mascot-notif-date').textContent = _notifDate(n.createdAt)

    const viewBtn = card.querySelector('.notif-view-btn')
    if (n.link) { viewBtn.textContent = I18n.t('commonView'); viewBtn.addEventListener('click', () => navigate(n.link)) }
    else card.querySelector('.mascot-notif-actions').remove()

    return card
}

function _buildLocalCard(n) {
    const card = _clone('tpl-finny-local-card')
    card.style.setProperty('--local-card-color', TYPE_COLOR[n.severity] ?? TYPE_COLOR.info)
    card.querySelector('.mascot-notif-title').textContent = n.message
    card.querySelector('.mascot-notif-date').textContent = _notifDate(n.createdAt)

    const viewBtn = card.querySelector('.local-view-btn')
    if (n.link) { viewBtn.textContent = I18n.t('commonView'); viewBtn.addEventListener('click', () => navigate(n.link)) }
    else card.querySelector('.mascot-notif-actions').remove()

    return card
}
