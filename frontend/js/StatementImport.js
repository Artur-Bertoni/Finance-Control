import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { doRequest, navigate, showQuickAdd, showToast } from '../utils/FrontendFunctions.js'
import { I18n } from './i18n.js'

let selectedFile  = null
let parsedRows    = []   // List<ParsedTransactionResponse> from /preview
let allCategories = []   // [{id, name}] loaded once
let allLocales    = []   // [{id, name}] loaded once

export function init() {
    SidebarManager.initialize()
    Account.addAccounts('account-input')
    loadCategories()
    loadLocales()

    const dropZone = document.getElementById('file-drop-zone')
    const fileInput = document.getElementById('file-input')
    const dropText  = document.getElementById('file-drop-text')

    dropZone.addEventListener('click', () => fileInput.click())

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            selectedFile = fileInput.files[0]
            setFileSelected(dropZone, dropText, selectedFile.name)
        }
    })

    dropZone.addEventListener('dragover', e => {
        e.preventDefault()
        dropZone.classList.add('drag-over')
    })
    dropZone.addEventListener('dragleave', () => dropZone.classList.remove('drag-over'))
    dropZone.addEventListener('drop', e => {
        e.preventDefault()
        dropZone.classList.remove('drag-over')
        const files = e.dataTransfer.files
        if (files.length > 0 && files[0].type === 'application/pdf') {
            selectedFile = files[0]
            setFileSelected(dropZone, dropText, selectedFile.name)
        }
    })

    document.getElementById('account-add-btn').addEventListener('click', () => {
        const fiOptions = (doRequest('/api/financial-institutions', 'GET') ?? [])
            .map(fi => ({ value: fi.id, label: fi.name }))

        showQuickAdd({
            title:  I18n.t('newAccount'),
            apiUrl: '/api/accounts',
            fields: [
                { id: 'name', label: `${I18n.t('accountName')} *`, type: 'text', required: true, placeholder: I18n.t('accountNamePlaceholder') },
                { id: 'fiId', label: `${I18n.t('financialInstitution')} *`, type: 'select', required: true, options: fiOptions,
                  placeholder: I18n.t('selectInstitution'),
                  addBtn: {
                    title: I18n.t('newFinancialInstitution'), apiUrl: '/api/financial-institutions',
                    fields: [
                        { id: 'name',    label: `${I18n.t('institutionName')} *`, type: 'text', required: true, placeholder: I18n.t('institutionNamePlaceholder') },
                        { id: 'address', label: I18n.t('institutionAddress'), type: 'text', placeholder: I18n.t('institutionAddressPlaceholder') },
                        { id: 'contact', label: I18n.t('institutionContact'),  type: 'text', placeholder: I18n.t('institutionContactPlaceholder') }
                    ]
                  }
                }
            ],
            onSuccess: account => {
                const sel = document.getElementById('account-input')
                const opt = document.createElement('option')
                opt.value = account.id
                opt.text  = account.name
                sel.appendChild(opt)
                sel.value = account.id
            }
        })
    })

    document.getElementById('cancel-btn').addEventListener('click', () => {
        document.body.classList.remove('review-mode')
        navigate('/pages/HomePage.html')
    })
    document.getElementById('analyze-btn').addEventListener('click', handleAnalyze)
    document.getElementById('review-back-btn').addEventListener('click', showUploadSection)
    document.getElementById('confirm-btn').addEventListener('click', handleConfirm)
    document.getElementById('select-all-check').addEventListener('change', function () {
        document.querySelectorAll('.row-check').forEach(cb => { cb.checked = this.checked })
        this.indeterminate = false
    })

    I18n.onChange(() => {
        if (!parsedRows.length) return
        document.querySelectorAll('.row-category-select').forEach(sel => {
            const blank = sel.querySelector('option[value=""]')
            if (blank) {
                blank.textContent = I18n.t('selectCategory')
                if (sel.value === '') { sel.selectedIndex = -1; sel.selectedIndex = 0 }
            }
            const newO = sel.querySelector('option[value="__new__"]')
            if (newO) newO.textContent = I18n.t('newCategoryInline')
        })
        document.querySelectorAll('.row-new-category-input').forEach(el => {
            el.placeholder = I18n.t('categoryNamePlaceholder')
        })
        document.querySelectorAll('.row-locale-select').forEach(sel => {
            const blank = sel.querySelector('option[value=""]')
            if (blank) {
                blank.textContent = I18n.t('selectLocale')
                if (sel.value === '') { sel.selectedIndex = -1; sel.selectedIndex = 0 }
            }
            const newO = sel.querySelector('option[value="__new__"]')
            if (newO) newO.textContent = I18n.t('newLocale') + '...'
        })
        document.querySelectorAll('.row-new-locale-input').forEach(el => {
            el.placeholder = I18n.t('localeNamePlaceholder')
        })
        document.querySelectorAll('.row-toggle-label').forEach(lbl => {
            lbl.title = I18n.t('shouldImportHint')
        })
    })
}

function loadCategories() {
    const data = doRequest('/api/categories', 'GET')
    allCategories = (data ?? []).map(c => ({ id: c.id, name: c.name }))
}

function loadLocales() {
    const data = doRequest('/api/transaction-locales', 'GET')
    allLocales = (data ?? []).map(l => ({ id: l.id, name: l.name }))
}

function setFileSelected(dropZone, dropText, name) {
    dropZone.classList.add('has-file')
    dropText.textContent = I18n.t('fileSelected', { name })
}

function showUploadSection() {
    document.body.classList.remove('review-mode')
    document.getElementById('upload-section').style.display = ''
    document.getElementById('review-section').style.display = 'none'
    document.getElementById('import-result').style.display  = 'none'
}

function handleAnalyze() {
    const accountId = document.getElementById('account-input').value
    if (!accountId) {
        showToast(I18n.t('fillRequiredFields', { fields: I18n.t('transactionAccount') }), 'warning')
        return
    }
    if (!selectedFile) {
        showToast(I18n.t('noFileSelected'), 'warning')
        return
    }

    const analyzeBtn = document.getElementById('analyze-btn')
    analyzeBtn.disabled = true
    const overlay = showOverlay()

    const formData = new FormData()
    formData.append('file', selectedFile)

    $.ajax({
        url:         '/api/statements/preview',
        type:        'POST',
        data:        formData,
        processData: false,
        contentType: false,
        success: rows => {
            parsedRows = rows
            if (rows.length === 0) {
                showToast(I18n.t('noTransactionsInPdf'), 'warning')
                return
            }
            buildReviewTable(rows)
            document.getElementById('upload-section').style.display = 'none'
            document.getElementById('review-section').style.display = ''
            document.body.classList.add('review-mode')
            SidebarManager.initTranslations()
        },
        error: xhr => {
            showToast(xhr.responseJSON?.message ?? I18n.t('importError'), 'error')
        },
        complete: () => {
            analyzeBtn.disabled = false
            overlay.remove()
        }
    })
}

function syncSelectAll() {
    const all  = document.querySelectorAll('.row-check')
    const checked = document.querySelectorAll('.row-check:checked')
    const headerCb = document.getElementById('select-all-check')
    headerCb.checked = all.length > 0 && checked.length === all.length
    headerCb.indeterminate = checked.length > 0 && checked.length < all.length
}

function buildReviewTable(rows) {
    const tbody = document.getElementById('review-tbody')
    tbody.innerHTML = ''

    rows.forEach((row, index) => {
        const tr = document.createElement('tr')
        tr.style.borderBottom = '1px solid var(--border)'
        tr.dataset.index = index

        const tdCheck = document.createElement('td')
        tdCheck.style.cssText = 'padding:8px 6px;text-align:center'
        const checkLabel = document.createElement('label')
        checkLabel.className = 'checkbox-label row-toggle-label'
        checkLabel.style.cssText = 'display:inline-flex;margin:0'
        checkLabel.title = I18n.t('shouldImportHint')
        checkLabel.dataset.i18nTitle = 'shouldImportHint'
        const check = document.createElement('input')
        check.type = 'checkbox'
        check.checked = true
        check.className = 'row-check'
        check.addEventListener('change', syncSelectAll)
        checkLabel.appendChild(check)
        tdCheck.appendChild(checkLabel)

        const tdDate = document.createElement('td')
        tdDate.style.cssText = 'padding:8px 6px;white-space:nowrap;color:var(--text-muted);font-size:13px'
        tdDate.textContent = formatDate(row.date)

        const tdDesc = document.createElement('td')
        tdDesc.style.cssText = 'padding:8px 6px;font-size:13px;max-width:220px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;cursor:help'
        tdDesc.title = row.description
        tdDesc.textContent = row.description

        const tdAmount = document.createElement('td')
        tdAmount.style.cssText = 'padding:8px 6px;white-space:nowrap;font-weight:600'
        tdAmount.style.color = row.type === 'credit' ? 'var(--color-credit, #22c55e)' : 'var(--color-debit, #ef4444)'
        tdAmount.textContent = (row.type === 'credit' ? '+ ' : '- ') + formatCurrency(row.amount)

        const tdType = document.createElement('td')
        tdType.style.padding = '8px 6px'
        const badge = document.createElement('span')
        badge.className = `tx-badge ${row.type}`
        badge.textContent = I18n.t(row.type)
        tdType.appendChild(badge)

        const tdCat = document.createElement('td')
        tdCat.style.padding = '6px'
        tdCat.appendChild(buildCategoryCell(row, index))

        const tdLocale = document.createElement('td')
        tdLocale.style.padding = '6px'
        tdLocale.appendChild(buildLocaleCell(index))

        tr.appendChild(tdCheck)
        tr.appendChild(tdDate)
        tr.appendChild(tdDesc)
        tr.appendChild(tdAmount)
        tr.appendChild(tdType)
        tr.appendChild(tdCat)
        tr.appendChild(tdLocale)
        tbody.appendChild(tr)
    })
}

function buildCategoryCell(row, index) {
    const wrapper = document.createElement('div')
    wrapper.style.cssText = 'display:flex;gap:6px;align-items:center'

    const select = document.createElement('select')
    select.className = 'row-category-select'
    select.dataset.index = index
    select.style.flex = '1'

    const blankOpt = document.createElement('option')
    blankOpt.value = ''
    blankOpt.textContent = I18n.t('selectCategory')
    blankOpt.dataset.i18n = 'selectCategory'
    select.appendChild(blankOpt)

    allCategories.forEach(cat => {
        const opt = document.createElement('option')
        opt.value = cat.id
        opt.textContent = cat.name
        if (row.suggestedCategoryId && String(row.suggestedCategoryId) === String(cat.id)) {
            opt.selected = true
        }
        select.appendChild(opt)
    })

    const newOpt = document.createElement('option')
    newOpt.value = '__new__'
    newOpt.textContent = I18n.t('newCategoryInline')
    newOpt.dataset.i18n = 'newCategoryInline'
    select.appendChild(newOpt)

    const newInput = document.createElement('input')
    newInput.type = 'text'
    newInput.className = 'row-new-category-input'
    newInput.placeholder = I18n.t('categoryNamePlaceholder')
    newInput.dataset.i18nPlaceholder = 'categoryNamePlaceholder'
    newInput.style.cssText = 'display:none;flex:1'

    select.addEventListener('change', () => {
        if (select.value === '__new__') {
            newInput.style.display = ''
            newInput.focus()
        } else {
            newInput.style.display = 'none'
        }
    })

    function createAndSelect() {
        const name = newInput.value.trim()
        if (!name) { select.value = ''; newInput.style.display = 'none'; return }

        $.ajax({
            url:         '/api/categories',
            type:        'POST',
            contentType: 'application/json',
            async:       false,
            data:        JSON.stringify({ name, aliases: [name] }),
            success: cat => {
                allCategories.push({ id: cat.id, name: cat.name })
                document.querySelectorAll('.row-category-select').forEach(sel => {
                    const opt = document.createElement('option')
                    opt.value = cat.id
                    opt.textContent = cat.name
                    sel.insertBefore(opt, sel.lastElementChild) // before "Nova categoria..."
                })
                select.value = cat.id
                newInput.style.display = 'none'
                newInput.value = ''
            },
            error: xhr => {
                showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingCategory'), 'error')
                select.value = ''
                newInput.style.display = 'none'
            }
        })
    }

    newInput.addEventListener('keydown', e => { if (e.key === 'Enter') createAndSelect() })
    newInput.addEventListener('blur', createAndSelect)

    wrapper.appendChild(select)
    wrapper.appendChild(newInput)
    return wrapper
}

function buildLocaleCell(index) {
    const select = document.createElement('select')
    select.className = 'row-locale-select'
    select.dataset.index = index
    select.style.width = '100%'

    const blankOpt = document.createElement('option')
    blankOpt.value = ''
    blankOpt.textContent = I18n.t('selectLocale')
    blankOpt.dataset.i18n = 'selectLocale'
    select.appendChild(blankOpt)

    allLocales.forEach(loc => {
        const opt = document.createElement('option')
        opt.value = loc.id
        opt.textContent = loc.name
        select.appendChild(opt)
    })

    const newOpt = document.createElement('option')
    newOpt.value = '__new__'
    newOpt.textContent = I18n.t('newLocale') + '...'
    select.appendChild(newOpt)

    const newInput = document.createElement('input')
    newInput.type = 'text'
    newInput.className = 'row-new-locale-input'
    newInput.placeholder = I18n.t('localeNamePlaceholder')
    newInput.dataset.i18nPlaceholder = 'localeNamePlaceholder'
    newInput.style.cssText = 'display:none;width:100%;margin-top:4px'

    select.addEventListener('change', () => {
        newInput.style.display = select.value === '__new__' ? '' : 'none'
        if (select.value === '__new__') newInput.focus()
    })

    function createAndSelect() {
        const name = newInput.value.trim()
        if (!name) { select.value = ''; newInput.style.display = 'none'; return }

        $.ajax({
            url:         '/api/transaction-locales',
            type:        'POST',
            contentType: 'application/json',
            async:       false,
            data:        JSON.stringify({ name }),
            success: loc => {
                allLocales.push({ id: loc.id, name: loc.name })
                document.querySelectorAll('.row-locale-select').forEach(sel => {
                    const opt = document.createElement('option')
                    opt.value = loc.id
                    opt.textContent = loc.name
                    sel.insertBefore(opt, sel.lastElementChild)
                })
                select.value = loc.id
                newInput.style.display = 'none'
                newInput.value = ''
            },
            error: xhr => {
                showToast(xhr.responseJSON?.message ?? I18n.t('errorSavingLocale'), 'error')
                select.value = ''
                newInput.style.display = 'none'
            }
        })
    }

    newInput.addEventListener('keydown', e => { if (e.key === 'Enter') createAndSelect() })
    newInput.addEventListener('blur', createAndSelect)

    const wrapper = document.createElement('div')
    wrapper.appendChild(select)
    wrapper.appendChild(newInput)
    return wrapper
}

function handleConfirm() {
    const accountId = document.getElementById('account-input').value
    const rows = []
    let missingCategory = false

    document.querySelectorAll('#review-tbody tr').forEach(tr => {
        const index     = Number(tr.dataset.index)
        const checked   = tr.querySelector('.row-check').checked
        const catSelect = tr.querySelector('.row-category-select')
        const categoryId = catSelect?.value && catSelect.value !== '__new__' ? Number(catSelect.value) : null

        if (checked && !categoryId) { missingCategory = true; return }

        const pr = parsedRows[index]
        const localeSelect = tr.querySelector('.row-locale-select')
        const localeId = localeSelect?.value && localeSelect.value !== '__new__'
            ? Number(localeSelect.value) : null

        rows.push({
            date:        pr.date,
            description: pr.description,
            amount:      pr.amount,
            type:        pr.type,
            categoryId:  checked ? categoryId : null,
            localeId:    checked ? localeId : null,
            skip:        !checked
        })
    })

    if (missingCategory) {
        showToast(I18n.t('missingCategoryForRows'), 'warning')
        return
    }

    const confirmBtn = document.getElementById('confirm-btn')
    confirmBtn.disabled = true
    const overlay = showOverlay()

    $.ajax({
        url:         '/api/statements/confirm',
        type:        'POST',
        contentType: 'application/json',
        data:        JSON.stringify({ accountId: Number(accountId), rows }),
        success: result => {
            document.body.classList.remove('review-mode')
            document.getElementById('review-section').style.display = 'none'
            const resultCard = document.getElementById('import-result')
            const resultText = document.getElementById('import-result-text')
            resultCard.style.display = ''
            resultText.textContent = I18n.t('importSuccess', { count: result.imported })
            resultCard.querySelector('.view-btn')?.remove()

            const viewBtn = document.createElement('button')
            viewBtn.className = 'btn btn-primary btn-sm view-btn'
            viewBtn.style.marginTop = '12px'
            viewBtn.textContent = I18n.t('viewImported')
            viewBtn.addEventListener('click', () => {
                sessionStorage.setItem('__homeFilters', JSON.stringify({
                    startDate: result.startDate ?? '',
                    endDate:   result.endDate   ?? '',
                    category:  '',
                    account:   accountId
                }))
                navigate('/pages/HomePage.html')
            })
            resultCard.appendChild(viewBtn)
            showToast(I18n.t('importSuccess', { count: result.imported }), 'success')
        },
        error: xhr => {
            showToast(xhr.responseJSON?.message ?? I18n.t('importError'), 'error')
        },
        complete: () => {
            confirmBtn.disabled = false
            overlay.remove()
        }
    })
}

function showOverlay() {
    const overlay = document.createElement('div')
    overlay.className = 'loading-overlay'
    overlay.innerHTML = '<div class="loading-spinner"></div>'
    document.body.appendChild(overlay)
    return overlay
}

function formatDate(isoDate) {
    if (!isoDate) return ''
    const [y, m, d] = isoDate.split('-')
    return `${d}/${m}/${y}`
}

function formatCurrency(value) {
    return Number(value).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

if (!globalThis.__appRouter) init()
