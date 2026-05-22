import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { doRequest, formatDate, navigate, showQuickAdd, showToast } from '../utils/FrontendFunctions.js'
import { I18n } from './i18n.js'
import { setupRequiredFieldValidation } from './utils/FieldValidation.js'
import { Icons } from './icons/IconLibrary.js'

let selectedFile  = null
let parsedRows    = []   // List<ParsedTransactionResponse> from /preview
let allCategories = []   // [{id, name}] loaded once
let allLocales    = []   // [{id, name}] loaded once

export function init() {
    SidebarManager.initialize()
    Account.addAccounts('account-input')
    loadCategories()
    loadLocales()
    setupRequiredFieldValidation(['account-input'])

    const dropZone = document.getElementById('file-drop-zone')
    const fileInput = document.getElementById('file-input')
    const dropText  = document.getElementById('file-drop-text')

    dropZone.addEventListener('click', () => fileInput.click())

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            selectedFile = fileInput.files[0]
            dropZone.classList.remove('field-error')
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
            dropZone.classList.remove('field-error')
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
                    ],
                    buildBody: v => ({ name: v.name, address: v.address || null, contact: v.contact || null })
                  }
                },
                { id: 'balance', label: I18n.t('currentBalance'), type: 'number', placeholder: '0.00', step: '0.01' }
            ],
            buildBody: v => ({
                name: v.name, financialInstitutionId: Number(v.fiId),
                balance: v.balance === '' ? 0 : Number(v.balance), contact: null, description: null
            }),
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
        })
        document.querySelectorAll('.row-locale-select').forEach(sel => {
            const blank = sel.querySelector('option[value=""]')
            if (blank) {
                blank.textContent = I18n.t('selectLocale')
                if (sel.value === '') { sel.selectedIndex = -1; sel.selectedIndex = 0 }
            }
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
    const dropZone  = document.getElementById('file-drop-zone')
    const missingFields = []

    if (!accountId) {
        document.getElementById('account-input').classList.add('field-error')
        missingFields.push(I18n.t('transactionAccount'))
    }
    if (!selectedFile) {
        dropZone.classList.add('field-error')
        missingFields.push(I18n.t('importFile'))
    }
    if (missingFields.length) {
        showToast(I18n.t('fillRequiredFields', { fields: missingFields.join(', ') }), 'warning')
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

    const addBtn = document.createElement('button')
    addBtn.type = 'button'
    addBtn.className = 'btn-add-inline'
    addBtn.title = I18n.t('newCategory')
    addBtn.innerHTML = Icons.add()
    addBtn.addEventListener('click', () => {
        showQuickAdd({
            title:  I18n.t('newCategory'),
            apiUrl: '/api/categories',
            fields: [
                { id: 'name',        label: `${I18n.t('categoryName')} *`, type: 'text', required: true, placeholder: I18n.t('categoryNamePlaceholder') },
                { id: 'description', label: I18n.t('categoryDescription'),  type: 'textarea', placeholder: I18n.t('categoryDescriptionPlaceholder') }
            ],
            buildBody: v => ({ name: v.name, description: v.description || null }),
            onSuccess: cat => {
                allCategories.push({ id: cat.id, name: cat.name })
                document.querySelectorAll('.row-category-select').forEach(sel => {
                    const opt = document.createElement('option')
                    opt.value = cat.id
                    opt.textContent = cat.name
                    sel.appendChild(opt)
                })
                select.value = cat.id
            }
        })
    })

    wrapper.appendChild(select)
    wrapper.appendChild(addBtn)
    return wrapper
}

function buildLocaleCell(index) {
    const wrapper = document.createElement('div')
    wrapper.style.cssText = 'display:flex;gap:6px;align-items:center'

    const select = document.createElement('select')
    select.className = 'row-locale-select'
    select.dataset.index = index
    select.style.flex = '1'

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

    const addBtn = document.createElement('button')
    addBtn.type = 'button'
    addBtn.className = 'btn-add-inline'
    addBtn.title = I18n.t('newLocation')
    addBtn.innerHTML = Icons.add()
    addBtn.addEventListener('click', () => {
        showQuickAdd({
            title:  I18n.t('newLocation'),
            apiUrl: '/api/transaction-locales',
            fields: [
                { id: 'name',    label: `${I18n.t('localeName')} *`, type: 'text', required: true, placeholder: I18n.t('localeNamePlaceholder') },
                { id: 'address', label: I18n.t('localeAddress'),      type: 'text', placeholder: I18n.t('localeAddressPlaceholder') }
            ],
            buildBody: v => ({ name: v.name, address: v.address || null }),
            onSuccess: loc => {
                allLocales.push({ id: loc.id, name: loc.name })
                document.querySelectorAll('.row-locale-select').forEach(sel => {
                    const opt = document.createElement('option')
                    opt.value = loc.id
                    opt.textContent = loc.name
                    sel.appendChild(opt)
                })
                select.value = loc.id
            }
        })
    })

    wrapper.appendChild(select)
    wrapper.appendChild(addBtn)
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


function formatCurrency(value) {
    return Number(value).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

if (!globalThis.__appRouter) init()
