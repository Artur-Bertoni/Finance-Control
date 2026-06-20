import { I18n } from '../i18n.js'
import { formatDate, formatCurrency } from '../../utils/FrontendFunctions.js'

export function openConflictModal(conflicts, allCategories, onConfirm) {
    const selections = new Map()
    conflicts.forEach(c => selections.set(c.rowIndex, String(c.currentCategoryId ?? c.suggestedCategoryId ?? '')))

    const overlay = document.createElement('div')
    overlay.className = 'modal-overlay'

    const card = document.createElement('div')
    card.className = 'modal-card'
    card.style.cssText = 'display:flex;flex-direction:column;width:min(92vw,580px);max-height:82vh;padding:0;overflow:hidden'

    const header = document.createElement('div')
    header.style.cssText = 'padding:20px 24px 14px;flex-shrink:0;border-bottom:1px solid var(--border)'

    const titleEl = document.createElement('p')
    titleEl.className = 'modal-title'
    titleEl.style.margin = '0 0 6px'
    titleEl.textContent = I18n.t('conflictResolutionTitle')

    const infoEl = document.createElement('p')
    infoEl.style.cssText = 'margin:0;font-size:13px;color:var(--text-muted);line-height:1.5'
    infoEl.textContent = I18n.t('conflictResolutionInfo')

    header.appendChild(titleEl)
    header.appendChild(infoEl)

    const body = document.createElement('div')
    body.style.cssText = 'flex:1;overflow-y:auto;padding:16px 24px;display:flex;flex-direction:column;gap:12px'

    conflicts.forEach(row => body.appendChild(_buildItem(row, allCategories, selections)))

    const footer = document.createElement('div')
    footer.style.cssText = 'padding:14px 24px 20px;flex-shrink:0;border-top:1px solid var(--border)'

    const btnRow = document.createElement('div')
    btnRow.className = 'modal-actions'

    const cancelBtn = document.createElement('button')
    cancelBtn.className = 'btn btn-secondary'
    cancelBtn.textContent = I18n.t('conflictCancel')
    cancelBtn.addEventListener('click', () => overlay.remove())

    const confirmBtn = document.createElement('button')
    confirmBtn.className = 'btn btn-primary'
    confirmBtn.textContent = I18n.t('conflictConfirm')
    confirmBtn.addEventListener('click', () => { overlay.remove(); onConfirm(selections) })

    btnRow.appendChild(cancelBtn)
    btnRow.appendChild(confirmBtn)
    footer.appendChild(btnRow)

    card.appendChild(header)
    card.appendChild(body)
    card.appendChild(footer)
    overlay.appendChild(card)
    document.body.appendChild(overlay)
}


function _setLblActive(lbl, active) {
    lbl.style.background = active ? 'var(--primary-bg,rgba(46,125,50,.12))' : 'transparent'
    lbl.style.color      = active ? 'var(--primary,#2e7d32)' : 'var(--text)'
    lbl.style.fontWeight = active ? '500' : '400'
    lbl.dataset.cfActive = active ? '1' : '0'

    const ind = lbl.querySelector('.cf-radio-ind')
    if (!ind) return
    ind.style.borderColor = active ? 'var(--primary,#2e7d32)' : 'var(--border,#ccc)'
    const dot = ind.querySelector('.cf-radio-dot')
    if (dot) dot.style.transform = active ? 'scale(1)' : 'scale(0)'
}


function _buildItem(row, allCategories, selections) {
    const { rowIndex } = row
    const suggestions   = row.allSuggestedCategories ?? []

    const item = document.createElement('div')
    item.style.cssText = 'border:1px solid var(--border);border-radius:8px;padding:14px'

    const descEl = document.createElement('div')
    descEl.style.cssText = 'font-size:13px;font-weight:600;color:var(--text);word-break:break-word;margin-bottom:4px'
    descEl.textContent = row.description

    const metaEl = document.createElement('div')
    metaEl.style.cssText = 'font-size:12px;color:var(--text-muted);margin-bottom:10px'
    const isCredit = (row.type ?? '').toLowerCase() === 'credit'
    metaEl.innerHTML =
        formatDate(row.date) + ' &nbsp;·&nbsp; ' +
        `<span style="color:${isCredit ? 'var(--color-credit,#22c55e)' : 'var(--color-debit,#ef4444)'};font-weight:600">` +
        `${isCredit ? '+' : '-'} ${formatCurrency(row.amount)}</span>` +
        ` &nbsp;·&nbsp; ${I18n.t((row.type ?? '').toLowerCase())}`

    const hint = document.createElement('div')
    hint.style.cssText = 'font-size:12px;color:var(--color-warning,#f59e0b);margin-bottom:10px'
    const hasManualSelection = row.currentCategoryId && String(row.currentCategoryId) !== String(row.suggestedCategoryId ?? '')
    if (hasManualSelection) {
        const manualCat = allCategories.find(c => String(c.id) === String(row.currentCategoryId))
        hint.textContent = I18n.t('manuallySelectedCategory', { name: manualCat?.name ?? '' })
    } else {
        hint.textContent = I18n.t('autoSelectedCategory', { name: row.suggestedCategoryName ?? '' })
    }

    const pillsRow = document.createElement('div')
    pillsRow.style.cssText = 'display:flex;flex-wrap:wrap;gap:8px'

    const setPill = (activeId) => {
        pillsRow.querySelectorAll('.cf-pill').forEach(pill => {
            const on = String(pill.dataset.catId) === String(activeId)
            pill.style.borderColor = on ? 'var(--primary,#2e7d32)'                   : 'var(--border)'
            pill.style.background  = on ? 'var(--primary-bg,rgba(46,125,50,.12))'    : 'transparent'
            pill.style.color       = on ? 'var(--primary,#2e7d32)'                   : 'var(--text)'
            pill.style.fontWeight  = on ? '600' : '400'
        })
        item.querySelectorAll('.cf-other-lbl').forEach(l => _setLblActive(l, false))
        item.querySelectorAll(`[name="cf-other-${rowIndex}"]`).forEach(r => { r.checked = false })
        selections.set(rowIndex, String(activeId))
    }

    suggestions.forEach(cat => {
        const pill = document.createElement('button')
        pill.type = 'button'
        pill.className = 'cf-pill'
        pill.dataset.catId = cat.id
        pill.style.cssText = 'padding:5px 14px;border-radius:20px;cursor:pointer;font-size:13px;border:2px solid var(--border);color:var(--text);transition:border-color .15s,background .15s,color .15s;background:transparent'
        pill.textContent = cat.name
        pill.addEventListener('click', () => setPill(cat.id))
        pillsRow.appendChild(pill)
    })

    const effectiveId = String(row.currentCategoryId ?? row.suggestedCategoryId ?? suggestions[0]?.id ?? '')
    const effectiveIsConflict = suggestions.some(s => String(s.id) === effectiveId)
    if (effectiveIsConflict) {
        setPill(effectiveId)
    }

    item.appendChild(descEl)
    item.appendChild(metaEl)
    item.appendChild(hint)
    item.appendChild(pillsRow)

    const otherCats = allCategories.filter(c => !suggestions.some(s => String(s.id) === String(c.id)))
    if (otherCats.length > 0) {
        item.appendChild(_buildOtherSection(rowIndex, otherCats, pillsRow, item, selections))
    }

    return item
}


function _buildOtherLabel(cat, rowIndex, isActive, listEl, pillsRow, selections) {
    const lbl = document.createElement('label')
    lbl.className = 'cf-other-lbl'
    lbl.style.cssText = [
        'display:flex', 'align-items:center', 'gap:10px',
        'padding:7px 10px', 'border-radius:6px', 'cursor:pointer',
        'font-size:13px', 'transition:background .12s,color .12s', 'user-select:none'
    ].join(';')

    const radio = document.createElement('input')
    radio.type    = 'radio'
    radio.name    = `cf-other-${rowIndex}`
    radio.value   = cat.id
    radio.checked = isActive
    radio.style.display = 'none'

    const ind = document.createElement('span')
    ind.className = 'cf-radio-ind'
    ind.style.cssText = [
        'width:16px', 'height:16px', 'border-radius:50%',
        'border:2px solid', 'flex-shrink:0',
        'display:flex', 'align-items:center', 'justify-content:center',
        'transition:border-color .15s'
    ].join(';')

    const dot = document.createElement('span')
    dot.className = 'cf-radio-dot'
    dot.style.cssText = [
        'width:8px', 'height:8px', 'border-radius:50%',
        'background:var(--primary,#2e7d32)',
        'transform:scale(0)', 'transition:transform .15s'
    ].join(';')
    ind.appendChild(dot)

    lbl.addEventListener('mouseenter', () => {
        if (lbl.dataset.cfActive !== '1') lbl.style.background = 'var(--primary-bg,rgba(46,125,50,.06))'
    })
    lbl.addEventListener('mouseleave', () => {
        if (lbl.dataset.cfActive !== '1') lbl.style.background = 'transparent'
    })

    radio.addEventListener('change', () => {
        listEl.querySelectorAll('.cf-other-lbl').forEach(l => _setLblActive(l, false))
        _setLblActive(lbl, true)
        pillsRow.querySelectorAll('.cf-pill').forEach(p => {
            p.style.borderColor = 'var(--border)'
            p.style.background  = 'transparent'
            p.style.color       = 'var(--text)'
            p.style.fontWeight  = '400'
        })
        selections.set(rowIndex, String(cat.id))
    })

    lbl.appendChild(radio)
    lbl.appendChild(ind)
    lbl.appendChild(document.createTextNode(cat.name))
    _setLblActive(lbl, isActive)
    return lbl
}


function _buildOtherSection(rowIndex, otherCats, pillsRow, item, selections) {
    const wrapper = document.createElement('div')
    wrapper.style.marginTop = '12px'

    const caret = document.createElement('i')
    caret.className = 'ph ph-caret-right'
    caret.style.cssText = 'font-size:11px;transition:transform .15s;color:var(--text-muted)'

    const toggle = document.createElement('button')
    toggle.type = 'button'
    toggle.style.cssText = 'background:none;border:none;cursor:pointer;font-size:12px;color:var(--text-muted);padding:0;display:inline-flex;align-items:center;gap:5px;font-weight:500'
    toggle.appendChild(caret)
    toggle.appendChild(document.createTextNode(' ' + I18n.t('otherCategory')))

    const content = document.createElement('div')
    content.style.cssText = 'display:none;margin-top:8px'

    let open = false
    toggle.addEventListener('click', () => {
        open = !open
        content.style.display   = open ? 'block' : 'none'
        caret.style.transform   = open ? 'rotate(90deg)' : ''
    })

    const searchInput = document.createElement('input')
    searchInput.type = 'text'
    searchInput.placeholder = I18n.t('searchPlaceholder')
    searchInput.style.cssText = [
        'width:100%', 'box-sizing:border-box', 'padding:7px 10px',
        'border-radius:6px', 'border:1px solid var(--border)',
        'background:var(--surface)', 'color:var(--text)',
        'font-size:12px', 'margin-bottom:6px', 'outline:none',
        'transition:border-color .15s'
    ].join(';')
    searchInput.addEventListener('focus',  () => { searchInput.style.borderColor = 'var(--primary,#2e7d32)' })
    searchInput.addEventListener('blur',   () => { searchInput.style.borderColor = 'var(--border)' })

    const listEl = document.createElement('div')
    listEl.style.cssText = [
        'max-height:164px', 'overflow-y:auto', 'display:flex', 'flex-direction:column',
        'gap:1px', 'border:1px solid var(--border)', 'border-radius:8px',
        'padding:4px', 'background:var(--surface)'
    ].join(';')

    const render = (filter = '') => {
        listEl.innerHTML = ''
        const currentId = String(selections.get(rowIndex) ?? '')
        const visible   = filter
            ? otherCats.filter(c => c.name.toLowerCase().includes(filter.toLowerCase()))
            : otherCats

        if (visible.length === 0) {
            const empty = document.createElement('span')
            empty.style.cssText = 'font-size:12px;color:var(--text-muted);padding:8px;display:block;text-align:center'
            empty.textContent = I18n.t('commonNoResults')
            listEl.appendChild(empty)
            return
        }

        visible.forEach(cat => {
            listEl.appendChild(_buildOtherLabel(cat, rowIndex, currentId === String(cat.id), listEl, pillsRow, selections))
        })
    }

    render()
    searchInput.addEventListener('input', () => render(searchInput.value))

    content.appendChild(searchInput)
    content.appendChild(listEl)
    wrapper.appendChild(toggle)
    wrapper.appendChild(content)
    return wrapper
}
