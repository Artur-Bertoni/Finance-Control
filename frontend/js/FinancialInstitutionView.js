import { doRequest, navigate, setBreadcrumb, showConfirm, showToast } from '../utils/FrontendFunctions.js'
import { FinancialInstitution } from './class/FinancialInstitutionClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { I18n } from './i18n.js'

export function init() {
    SidebarManager.initialize()

    const fiId = new URLSearchParams(globalThis.location.search).get('id')
    if (!fiId) { navigate('/pages/FinancialInstitutionDashboard.html'); return }

    const response = doRequest(`/api/financial-institutions/${fiId}`, 'GET')
    if (!response?.id) { navigate('/pages/FinancialInstitutionDashboard.html'); return }

    const fi = FinancialInstitution.processFinancialInstitution(response)

    setBreadcrumb([
        { i18nKey: 'financialInstitutions', url: '/pages/FinancialInstitutionDashboard.html' },
        { label: fi.name }
    ])

    document.getElementById('detail-name').textContent = fi.name

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
        navigate(`/pages/FinancialInstitution.html?id=${fiId}`)
    )

    document.getElementById('delete-btn').addEventListener('click', () => {
        showConfirm(I18n.t('deleteConfirm'), () => {
            $.ajax({
                url:   `/api/financial-institutions/${fiId}`,
                type:  'DELETE',
                async: false,
                success: () => navigate('/pages/FinancialInstitutionDashboard.html'),
                error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingInstitution'), 'error')
            })
        })
    })
}

if (!globalThis.__appRouter) init()
