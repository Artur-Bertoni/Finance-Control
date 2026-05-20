import {
    addDeleteIcon, clearDirtyGuard, doRequest, navigate, navigateWithToast,
    setBreadcrumb, setupDirtyGuard, showConfirm, showToast
} from '../../utils/FrontendFunctions.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { setupRequiredFieldValidation, validateRequiredFields } from '../utils/FieldValidation.js'
import { InputMasks } from '../utils/InputMasks.js'
import { I18n } from '../i18n.js'

const REQUIRED = ['name-input', 'type-select', 'target-input', 'start-date-input']

export function init() {
    SidebarManager.initialize()
    setupRequiredFieldValidation(REQUIRED)

    const params = new URLSearchParams(globalThis.location.search)
    const goalId = params.get('id')

    InputMasks.money(document.getElementById('target-input'))

    initDatePickers(goalId)
    loadDropdownData()

    if (goalId) {
        loadGoal(goalId)
    }

    document.getElementById('type-select')?.addEventListener('change', toggleExceedRow)
    toggleExceedRow()

    document.getElementById('cancel-btn')?.addEventListener('click', () =>
        navigate('/pages/lists/GoalList.html')
    )

    document.getElementById('save-btn')?.addEventListener('click', () => handleSave(goalId))

    setupDirtyGuard()
    I18n.onChange(() => { refreshI18nSelectOptions(); InputMasks.reformatAll() })
}

const FLATPICKR_LOCALES = { pt: 'pt', es: 'es' }

function initDatePickers(goalId) {
    if (typeof flatpickr === 'undefined') return
    const lang   = I18n.getLanguage()
    const locale = FLATPICKR_LOCALES[lang]
        ? (flatpickr.l10ns?.[FLATPICKR_LOCALES[lang]] ?? undefined)
        : undefined
    const altFormat = lang === 'en' ? 'm/d/Y' : 'd/m/Y'
    const base = {
        dateFormat:    'Y-m-d',
        altInput:      true,
        altFormat,
        altInputClass: 'flatpickr-input fc-date-input',
        disableMobile: true,
        allowInput:    false,
        ...(locale ? { locale } : {}),
    }
    const tomorrow = new Date(); tomorrow.setDate(tomorrow.getDate() + 1)
    flatpickr('#start-date-input', { ...base, ...(goalId ? {} : { defaultDate: tomorrow }) })
    flatpickr('#end-date-input',   base)
}

function loadDropdownData() {
    const categories = doRequest('/api/categories', 'GET') ?? []
    const locales    = doRequest('/api/transaction-locales', 'GET') ?? []
    renderMultiCheckList('categories-multi', categories, [])
    renderMultiCheckList('locales-multi',    locales,    [])
}

function loadGoal(goalId) {
    const goal = doRequest(`/api/goals/${goalId}`, 'GET')
    if (!goal?.id) { navigate('/pages/lists/GoalList.html'); return }

    const titleEl = document.getElementById('page-title-text')
    if (titleEl) {
        titleEl.dataset.i18n = 'editGoal'
        titleEl.textContent  = I18n.t('editGoal')
    }
    const saveBtn = document.getElementById('save-btn')
    if (saveBtn) { saveBtn.dataset.i18n = 'saveChanges'; saveBtn.textContent = I18n.t('saveChanges') }

    setBreadcrumb([
        { i18nKey: 'goals', url: '/pages/lists/GoalList.html' },
        { label: goal.name, url: `/pages/lists/GoalList.html` },
        { i18nKey: 'edit' }
    ])

    document.getElementById('name-input').value        = goal.name ?? ''
    document.getElementById('description-input').value = goal.description ?? ''
    document.getElementById('target-input').value = goal.targetAmount ?? ''

    const typeSelect = document.getElementById('type-select')
    typeSelect.value = goal.type ?? ''
    toggleExceedRow()

    setFlatpickrDate('start-date-input', goal.startDate)
    setFlatpickrDate('end-date-input',   goal.endDate)

    setCheck('notify-50',       goal.notifyAt50)
    setCheck('notify-75',       goal.notifyAt75)
    setCheck('notify-90',       goal.notifyAt90)
    setCheck('notify-complete', goal.notifyOnComplete)
    setCheck('notify-deadline', goal.notifyOnDeadline)
    setCheck('notify-exceed',   goal.notifyOnExceed)

    const selectedCatIds = (goal.categories ?? []).map(c => c.id)
    const selectedLocIds = (goal.locales     ?? []).map(l => l.id)
    const categories = doRequest('/api/categories', 'GET') ?? []
    const locales    = doRequest('/api/transaction-locales', 'GET') ?? []
    renderMultiCheckList('categories-multi', categories, selectedCatIds)
    renderMultiCheckList('locales-multi',    locales,    selectedLocIds)

    const deleteBtn = addDeleteIcon()
    deleteBtn.addEventListener('click', () => {
        showConfirm(I18n.t('goalDeleteConfirm'), () => {
            $.ajax({
                url: `/api/goals/${goalId}`, type: 'DELETE', async: false,
                success: () => { clearDirtyGuard(); navigateWithToast('/pages/lists/GoalList.html', I18n.t('goalDeletedSuccess'), 'success') },
                error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingGoal'), 'error')
            })
        }, I18n.t('confirmAction'))
    })

    const archiveBtn = document.createElement('button')
    archiveBtn.className    = 'btn btn-secondary btn-sm'
    archiveBtn.type         = 'button'
    archiveBtn.dataset.i18n = 'archiveGoal'
    archiveBtn.textContent  = I18n.t('archiveGoal')
    archiveBtn.addEventListener('click', () => {
        showConfirm(I18n.t('goalArchiveConfirm'), () => {
            $.ajax({
                url: `/api/goals/${goalId}/archive`, type: 'PUT', async: false,
                success: () => { clearDirtyGuard(); navigateWithToast('/pages/lists/GoalList.html', I18n.t('goalArchivedSuccess'), 'success') },
                error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorArchivingGoal'), 'error')
            })
        }, I18n.t('confirmAction'))
    })
    document.getElementById('header-actions')?.prepend(archiveBtn)
}

function handleSave(goalId) {
    const fieldLabels = {
        'name-input':       I18n.t('goalName'),
        'type-select':      I18n.t('goalType'),
        'target-input':     I18n.t('goalTargetAmount'),
        'start-date-input': I18n.t('goalStartDate'),
    }
    const empty = validateRequiredFields(REQUIRED, fieldLabels)
    if (empty.length) {
        showToast(I18n.t('fillRequiredFields', { fields: empty.join(', ') }), 'warning')
        return
    }

    const body = {
        name:            document.getElementById('name-input').value,
        description:     document.getElementById('description-input').value || null,
        type:            document.getElementById('type-select').value,
        targetAmount:    Number.parseFloat(document.getElementById('target-input').value),
        startDate:        getInputValue('start-date-input'),
        endDate:          getInputValue('end-date-input') || null,
        categoryIds:      getCheckedIds('categories-multi'),
        localeIds:        getCheckedIds('locales-multi'),
        notifyAt50:       getCheck('notify-50'),
        notifyAt75:       getCheck('notify-75'),
        notifyAt90:       getCheck('notify-90'),
        notifyOnComplete: getCheck('notify-complete'),
        notifyOnDeadline: getCheck('notify-deadline'),
        notifyOnExceed:   getCheck('notify-exceed'),
    }

    $.ajax({
        url:         goalId ? `/api/goals/${goalId}` : '/api/goals',
        type:        goalId ? 'PUT' : 'POST',
        async:       false,
        contentType: 'application/json',
        data:        JSON.stringify(body),
        success: (data) => {
            clearDirtyGuard()
            const id = goalId ?? data?.id
            if (goalId) {
                navigate(`/pages/views/GoalView.html?id=${id}`)
            } else {
                navigateWithToast('/pages/lists/GoalList.html', I18n.t('goalCreatedSuccess'), 'success', id ? `/pages/views/GoalView.html?id=${id}` : null)
            }
        },
        error: xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingGoal'), 'error')
    })
}

function renderMultiCheckList(containerId, items, selectedIds) {
    const container = document.getElementById(containerId)
    if (!container) return
    container.innerHTML = ''
    if (!items.length) {
        container.innerHTML = `<span class="text-muted" style="font-size:13px">${I18n.t('noResults')}</span>`
        return
    }
    for (const item of items) {
        const label = document.createElement('label')
        label.className = 'checkbox-label'
        const cb = document.createElement('input')
        cb.type    = 'checkbox'
        cb.value   = item.id
        cb.checked = selectedIds.includes(item.id)
        const span = document.createElement('span')
        span.textContent = item.name
        label.appendChild(cb)
        label.appendChild(span)
        container.appendChild(label)
    }
}

function getCheckedIds(containerId) {
    return [...document.querySelectorAll(`#${containerId} input[type="checkbox"]:checked`)]
        .map(cb => Number.parseInt(cb.value, 10))
}

function getCheck(id) {
    return document.getElementById(id)?.checked ?? true
}

function setCheck(id, value) {
    const el = document.getElementById(id)
    if (el) el.checked = value !== false
}

function getInputValue(id) {
    // flatpickr stores ISO value in the original hidden input
    return document.getElementById(id)?._flatpickr?.selectedDates?.[0]
        ? document.getElementById(id)._flatpickr.formatDate(
              document.getElementById(id)._flatpickr.selectedDates[0], 'Y-m-d')
        : (document.getElementById(id)?.value ?? '')
}

function setFlatpickrDate(id, dateStr) {
    const fp = document.getElementById(id)?._flatpickr
    if (fp && dateStr) fp.setDate(dateStr, false, 'Y-m-d')
}

function toggleExceedRow() {
    const type = document.getElementById('type-select')?.value
    const row  = document.getElementById('notify-exceed-row')
    if (!row) return
    row.style.display = type === 'expense_limit' ? '' : 'none'
}

function refreshI18nSelectOptions() {
    const typeMap = {
        '':              I18n.t('goalSelectType'),
        'expense_limit': I18n.t('goalTypeExpenseLimit'),
        'savings':       I18n.t('goalTypeSavings'),
        'income':        I18n.t('goalTypeIncome'),
    }
    const sel = document.getElementById('type-select')
    if (!sel) return
    const current = sel.value
    for (const opt of sel.options) {
        if (typeMap[opt.value] !== undefined) opt.textContent = typeMap[opt.value]
    }
    sel.value = current
}

if (!globalThis.__appRouter) init()
