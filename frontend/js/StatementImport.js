import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { doRequest, navigate, showQuickAdd, showToast } from '../utils/FrontendFunctions.js'
import { I18n } from './i18n.js'

let selectedFile = null

export function init() {
    SidebarManager.initialize()

    Account.addAccounts('account-input')

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

    document.getElementById('cancel-btn').addEventListener('click', () => navigate('/pages/HomePage.html'))
    document.getElementById('import-btn').addEventListener('click', handleImport)
}

function setFileSelected(dropZone, dropText, name) {
    dropZone.classList.add('has-file')
    dropText.textContent = I18n.t('fileSelected', { name })
}

function handleImport() {
    const accountId = document.getElementById('account-input').value

    if (!accountId) {
        showToast(I18n.t('fillRequiredFields', { fields: I18n.t('transactionAccount') }), 'warning')
        return
    }
    if (!selectedFile) {
        showToast(I18n.t('noFileSelected'), 'warning')
        return
    }

    const formData = new FormData()
    formData.append('file', selectedFile)
    formData.append('accountId', accountId)

    const importBtn = document.getElementById('import-btn')
    importBtn.disabled = true

    const overlay = document.createElement('div')
    overlay.className = 'loading-overlay'
    overlay.innerHTML = '<div class="loading-spinner"></div>'
    document.body.appendChild(overlay)

    $.ajax({
        url:         '/api/statements/import',
        type:        'POST',
        data:        formData,
        processData: false,
        contentType: false,
        success: result => {
            const accountId  = document.getElementById('account-input').value
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
            importBtn.disabled = false
            overlay.remove()
        }
    })
}

if (!globalThis.__appRouter) init()
