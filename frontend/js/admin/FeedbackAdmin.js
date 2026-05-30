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

    setBreadcrumb([{ label: I18n.t('adminFeedbacksTitle'), url: '/pages/admin/FeedbackAdmin.html' }])

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
        : '—'
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

    if (!items.length) {
        list.innerHTML = `<div class="empty-state"><p>${I18n.t('noSearchResults', { query: '' })}</p></div>`
        return
    }

    list.innerHTML = items.map(f => {
        const typeLabel = _typeLabel(f.type)
        const typeCls   = _typeCls(f.type)
        const npsText   = f.npsScore == null
            ? ''
            : `<span class="feedback-nps-badge">${f.npsScore}<span style="font-weight:400;opacity:.6">/10</span></span>`
        const date      = formatDateTime(f.createdAt)
        const msgSafe   = _escapeHtml(f.message)

        return `
        <div class="feedback-admin-card">
          <div class="feedback-admin-card-header">
            <div class="feedback-admin-meta">
              <strong>${_escapeHtml(f.userName)}</strong>
              <span class="feedback-admin-email">${_escapeHtml(f.userEmail)}</span>
              <span class="feedback-admin-date">${date}</span>
            </div>
            <div class="feedback-admin-badges">
              <span class="badge badge--${typeCls}">${typeLabel}</span>
              ${npsText}
            </div>
          </div>
          <p class="feedback-admin-message">${msgSafe}</p>
        </div>`
    }).join('')
}

function _renderPagination(data) {
    const container = document.getElementById('feedbacks-pagination')
    if (!container) return

    const total = data.totalPages ?? 1
    container.hidden = total <= 1

    if (total <= 1) return

    const prev = _page > 0
    const next = _page < total - 1

    container.innerHTML = `
      <button class="btn btn-ghost btn-sm" id="pg-prev" ${prev ? '' : 'disabled'}>‹</button>
      <span>${I18n.t('commonPageOf', { page: _page + 1, total })}</span>
      <button class="btn btn-ghost btn-sm" id="pg-next" ${next ? '' : 'disabled'}>›</button>`

    document.getElementById('pg-prev')?.addEventListener('click', () => { _page--; _load() })
    document.getElementById('pg-next')?.addEventListener('click', () => { _page++; _load() })
}

function _typeLabel(type) {
    return { SUGGESTION: I18n.t('feedbackTypeSuggestion'), BUG: I18n.t('feedbackTypeBug'), GENERAL: I18n.t('feedbackTypeGeneral') }[type] ?? type
}

function _typeCls(type) {
    return { SUGGESTION: 'info', BUG: 'danger', GENERAL: 'neutral' }[type] ?? 'neutral'
}

function _escapeHtml(str) {
    return (str ?? '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;')
}
