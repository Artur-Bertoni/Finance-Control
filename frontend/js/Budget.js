import { doRequest, formatMoney, showToast, showConfirm, initFilterToggle } from '../utils/FrontendFunctions.js'
import { SidebarManager } from './components/SidebarManager.js'
import { I18n } from './i18n.js'

let budgets    = []
let categories = []

export function init() {
    document.body.classList.add('page-budget')
    SidebarManager.initialize()
    loadData()
    document.getElementById('budget-save-btn')?.addEventListener('click', saveBudget)
    initFilterToggle(() => false)
    I18n.onChange(renderAll)
}

function openForm() {
    const section = document.getElementById('budget-form-section')
    section?.classList.add('open')
    section?.querySelector('.filter-toggle-btn')?.setAttribute('aria-expanded', 'true')
}

function loadData() {
    budgets    = doRequest('/api/budgets', 'GET') ?? []
    categories = doRequest('/api/categories', 'GET') ?? []
    renderAll()
}

function renderAll() {
    populateCategorySelect()
    renderList()
}

function populateCategorySelect() {
    const sel = document.getElementById('budget-category')
    if (!sel) return
    const current = sel.value
    const budgetedIds = new Set(budgets.map(b => b.categoryId))
    sel.innerHTML = `<option value="" disabled ${current ? '' : 'selected'}>${I18n.t('selectCategory')}</option>`
    for (const c of categories) {
        const opt = document.createElement('option')
        opt.value = c.id
        opt.textContent = budgetedIds.has(c.id) ? `${c.name} (${I18n.t('budgetAlreadySet')})` : c.name
        sel.appendChild(opt)
    }
    if (current) sel.value = current
}

function saveBudget() {
    const sel        = document.getElementById('budget-category')
    const limitInput = document.getElementById('budget-limit')
    const categoryId   = Number(sel.value)
    const monthlyLimit = Number(limitInput.value)

    if (!categoryId)                      { showToast(I18n.t('budgetSelectCategory'), 'warning'); return }
    if (!monthlyLimit || monthlyLimit <= 0) { showToast(I18n.t('budgetInvalidLimit'), 'warning'); return }

    let result = null
    $.ajax({
        url: '/api/budgets', type: 'POST', async: false, contentType: 'application/json',
        data: JSON.stringify({ categoryId, monthlyLimit }),
        success: () => { result = { ok: true } },
        error:   xhr => { result = { ok: false, xhr } }
    })
    if (!result.ok) {
        showToast(result.xhr.responseJSON?.message ?? I18n.t('errorGeneric'), 'error')
        return
    }

    sel.value = ''
    limitInput.value = ''
    showToast(I18n.t('budgetSavedSuccess'), 'success')
    loadData()
}

function renderList() {
    const list = document.getElementById('budget-list')
    if (!list) return
    list.innerHTML = ''

    if (!budgets.length) {
        const empty = document.createElement('div')
        empty.className = 'empty-state'
        empty.innerHTML = `<p>${I18n.t('budgetEmpty')}</p>`
        list.appendChild(empty)
        return
    }

    for (const b of budgets) list.appendChild(buildItem(b))
}

function buildItem(b) {
    const item = document.getElementById('tpl-budget-item').content.firstElementChild.cloneNode(true)

    const icon = item.querySelector('.budget-item-icon')
    if (b.categoryIconKey) { icon.classList.add(b.categoryIconKey); icon.hidden = false }
    item.querySelector('.budget-item-name').textContent = b.categoryName

    const pct  = b.percent ?? 0
    const fill = item.querySelector('.budget-item-bar-fill')
    fill.style.width      = `${Math.min(pct, 100)}%`
    fill.style.background  = barColor(pct)

    item.querySelector('.budget-item-spent').textContent =
        `${formatMoney(b.spent ?? 0)} / ${formatMoney(b.monthlyLimit ?? 0)}`
    const percentEl = item.querySelector('.budget-item-percent')
    percentEl.textContent = `${Math.round(pct)}%`
    if (pct > 100) percentEl.classList.add('budget-over')

    item.querySelector('.budget-edit-btn').addEventListener('click', () => startEdit(b))
    item.querySelector('.budget-delete-btn').addEventListener('click', () => removeBudget(b))
    return item
}

function barColor(pct) {
    if (pct >= 100) return '#EF4444'
    if (pct >= 90)  return '#F97316'
    if (pct >= 75)  return '#EAB308'
    return 'var(--primary)'
}

function startEdit(b) {
    openForm()
    const sel        = document.getElementById('budget-category')
    const limitInput = document.getElementById('budget-limit')
    sel.value        = b.categoryId
    limitInput.value = b.monthlyLimit
    sel.scrollIntoView({ behavior: 'smooth', block: 'center' })
    limitInput.focus()
}

function removeBudget(b) {
    showConfirm(I18n.t('budgetDeleteConfirm', { category: b.categoryName }), () => {
        $.ajax({
            url: `/api/budgets/${b.id}`, type: 'DELETE', async: false,
            success: () => { showToast(I18n.t('budgetDeletedSuccess'), 'success'); loadData() },
            error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorGeneric'), 'error')
        })
    }, I18n.t('confirmAction'))
}

if (!globalThis.__appRouter) init()
