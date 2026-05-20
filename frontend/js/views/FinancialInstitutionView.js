import { doRequest, navigate, setBreadcrumb } from '../../utils/FrontendFunctions.js'
import { FinancialInstitution } from '../class/FinancialInstitutionClass.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { ChangeHistoryManager } from '../components/ChangeHistoryManager.js'
import { I18n } from '../i18n.js'

export function init() {
    SidebarManager.initialize()

    const fiId = new URLSearchParams(globalThis.location.search).get('id')
    if (!fiId) { navigate('/pages/lists/FinancialInstitutionList.html'); return }

    const response = doRequest(`/api/financial-institutions/${fiId}`, 'GET')
    if (!response?.id) { navigate('/pages/lists/FinancialInstitutionList.html'); return }

    const fi = FinancialInstitution.processFinancialInstitution(response)

    setBreadcrumb([
        { i18nKey: 'financialInstitutions', url: '/pages/lists/FinancialInstitutionList.html' },
        { label: fi.name }
    ])

    document.getElementById('detail-name').textContent = fi.name

    if (fi.iconKey) {
        const iconRow = document.getElementById('icon-row')
        const iconEl  = document.getElementById('detail-icon')
        if (iconRow) iconRow.style.display = ''
        if (iconEl)  iconEl.innerHTML = `<i class="ph ${fi.iconKey}"></i>`
    }

    const addressEl = document.getElementById('detail-address')
    if (fi.address) {
        addressEl.textContent = fi.address
    } else {
        addressEl.innerHTML = `<span class="detail-empty">${I18n.t('notInformed')}</span>`
    }

    const contactEl = document.getElementById('detail-contact')
    if (fi.contact) {
        contactEl.textContent = fi.contact
    } else {
        contactEl.innerHTML = `<span class="detail-empty">${I18n.t('notInformed')}</span>`
    }

    document.getElementById('edit-btn').addEventListener('click', () =>
        navigate(`/pages/crud/FinancialInstitution.html?id=${fiId}`)
    )

    let historyLoaded = false
    document.querySelectorAll('.view-tab').forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab
            document.querySelectorAll('.view-tab').forEach(b => b.classList.remove('view-tab--active'))
            btn.classList.add('view-tab--active')
            document.getElementById('tab-details').style.display = tab === 'details' ? '' : 'none'
            document.getElementById('tab-history').style.display  = tab === 'history'  ? '' : 'none'
            if (tab === 'history' && !historyLoaded) {
                historyLoaded = true
                ChangeHistoryManager.loadAndRender('financial_institution', fiId, response.createdAt, 'history-container')
            }
        })
    })
}

if (!globalThis.__appRouter) init()
