import { I18n }            from '../i18n.js'
import { SidebarManager }  from '../components/SidebarManager.js'
import { setBreadcrumb, doRequest, formatDateTime, navigate, initFilterToggle } from '../../utils/FrontendFunctions.js'

const PAGE_SIZE = 20

let _page        = 0
let _typeFilter  = ''
let _filterToggle = null

export async function init() {
    await SidebarManager.initialize()

    if (!globalThis.__currentUser?.admin) {
        navigate('/pages/HomePage.html')
        return
    }

    setBreadcrumb([
        { label: I18n.t('adminPanel'), url: '/pages/admin/Admin.html' },
        { label: I18n.t('adminFeedbacksTitle') }
    ])

    _filterToggle = initFilterToggle(() => !!_typeFilter)

    document.getElementById('type-filter')?.addEventListener('change', e => {
        _typeFilter = e.target.value
        _page = 0
        _filterToggle?.syncActive()
        _syncClearBtn()
        _load()
    })

    document.getElementById('clear-filter-btn')?.addEventListener('click', () => {
        const select = document.getElementById('type-filter')
        if (select) select.value = ''
        _typeFilter = ''
        _page = 0
        _filterToggle?.syncActive()
        _syncClearBtn()
        _load()
    })

    _load()
}

function _syncClearBtn() {
    const wrapper = document.getElementById('clear-filter-btn')?.closest('.filter-clear-field')
    if (wrapper) wrapper.style.display = _typeFilter ? '' : 'none'
}

function _load() {
    let url = `/api/admin/feedbacks?page=${_page}&size=${PAGE_SIZE}`
    if (_typeFilter) url += `&type=${_typeFilter}`

    const data = doRequest(url, 'GET')
    if (!data) return

    _renderStats(data)
    _renderList(data.content ?? [])
    _renderPagination(data)
}

function _renderStats(data) {
    const items       = data.content ?? []
    const total       = data.totalElements ?? 0
    const withNps     = items.filter(f => f.npsScore != null)
    const avgNps      = withNps.length > 0
        ? (withNps.reduce((s, f) => s + f.npsScore, 0) / withNps.length).toFixed(1)
        : '-'
    const suggestions = items.filter(f => f.type === 'SUGGESTION').length
    const bugs        = items.filter(f => f.type === 'BUG').length

    const set = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val }
    set('stat-total',       total)
    set('stat-avg-nps',     avgNps)
    set('stat-suggestions', suggestions)
    set('stat-bugs',        bugs)
}

function _renderList(items) {
    const list = document.getElementById('feedbacks-list')
    if (!list) return

    list.innerHTML = ''
    if (!items.length) {
        const empty = document.getElementById('tpl-feedback-empty').content.firstElementChild.cloneNode(true)
        empty.querySelector('p').textContent = I18n.t('noSearchResults', { query: '' })
        list.appendChild(empty)
        return
    }

    for (const f of items) list.appendChild(_buildFeedbackCard(f))
}

function _buildFeedbackCard(f) {
    const card = document.getElementById('tpl-feedback-card').content.firstElementChild.cloneNode(true)
    card.querySelector('.fb-user').textContent  = f.userName
    card.querySelector('.fb-email').textContent = f.userEmail
    card.querySelector('.fb-date').textContent  = formatDateTime(f.createdAt)

    const typeBadge = card.querySelector('.fb-type')
    typeBadge.classList.add(`badge--${_typeCls(f.type)}`)
    typeBadge.textContent = _typeLabel(f.type)

    if (f.npsScore != null) {
        const nps = card.querySelector('.fb-nps')
        nps.querySelector('.fb-nps-score').textContent = f.npsScore
        nps.hidden = false
    }

    card.querySelector('.fb-message').textContent = f.message
    return card
}

function _renderPagination(data) {
    const container = document.getElementById('feedbacks-pagination')
    if (!container) return

    const total = data.totalPages ?? 1
    container.innerHTML = ''
    container.hidden = total <= 1
    if (total <= 1) return

    container.appendChild(document.getElementById('tpl-feedback-pagination').content.cloneNode(true))
    const prevBtn = container.querySelector('.pg-prev')
    const nextBtn = container.querySelector('.pg-next')

    prevBtn.disabled = _page === 0
    prevBtn.addEventListener('click', () => { _page--; _load() })
    nextBtn.disabled = _page >= total - 1
    nextBtn.addEventListener('click', () => { _page++; _load() })
    container.querySelector('.pg-label').textContent = I18n.t('commonPageOf', { page: _page + 1, total })
}

function _typeLabel(type) {
    return { SUGGESTION: I18n.t('feedbackTypeSuggestion'), BUG: I18n.t('feedbackTypeBug'), GENERAL: I18n.t('feedbackTypeGeneral') }[type] ?? type
}

function _typeCls(type) {
    return { SUGGESTION: 'info', BUG: 'danger', GENERAL: 'neutral' }[type] ?? 'neutral'
}
