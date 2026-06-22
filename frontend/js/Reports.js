import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { I18n } from './i18n.js'
import { initFilterToggle, showToast } from '../utils/FrontendFunctions.js'
import { showOverlay } from './modals/LoadingOverlay.js'

export function init() {
    SidebarManager.initialize()
    Account.addAccounts('account-input')
    initFilterToggle(isFilterActive)

    document.getElementById('export-pdf-btn')?.addEventListener('click', () => exportReport('pdf'))
    document.getElementById('export-excel-btn')?.addEventListener('click', () => exportReport('excel'))
}

function isFilterActive() {
    const period  = document.getElementById('period-input')?.value  ?? '1m'
    const account = document.getElementById('account-input')?.value ?? ''
    return !(period === '1m' && account === '')
}

function getPeriodDates() {
    const period = document.getElementById('period-input')?.value ?? '1m'
    const today = new Date()
    const todayStr = toDateStr(today)

    if (period === 'ytd') {
        return { startDate: `${today.getFullYear()}-01-01`, endDate: todayStr }
    }
    const months = period === '1m' ? 1 : period === '3m' ? 3 : period === '6m' ? 6 : 12
    const start = new Date(today.getFullYear(), today.getMonth() - months, 1)
    return { startDate: toDateStr(start), endDate: todayStr }
}

function toDateStr(d) {
    return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().split('T')[0]
}

async function exportReport(format) {
    const { startDate, endDate } = getPeriodDates()
    const accountId = document.getElementById('account-input')?.value ?? ''

    const params = new URLSearchParams({ startDate, endDate, lang: I18n.getLanguage() })
    if (accountId) params.append('accountId', accountId)

    const overlay = showOverlay()
    try {
        const res = await fetch(`/api/reports/export/${format}?${params}`, { credentials: 'same-origin' })
        if (!res.ok) throw new Error(`HTTP ${res.status}`)

        const blob = await res.blob()
        const ext  = format === 'pdf' ? 'pdf' : 'xlsx'
        triggerDownload(blob, `relatorio-financeiro-${startDate}_${endDate}.${ext}`)
    } catch {
        showToast(I18n.t('reportError'), 'error')
    } finally {
        overlay.remove()
    }
}

function triggerDownload(blob, filename) {
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
}

if (!globalThis.__appRouter) init()
