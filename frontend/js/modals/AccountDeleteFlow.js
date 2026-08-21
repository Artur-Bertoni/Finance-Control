import { navigate, navigateWithToast, showToast } from '../../utils/FrontendFunctions.js'
import { showConfirm, showConfirmBar } from './ConfirmModal.js'
import { buildDeleteMessage, fetchDeleteImpact } from './DeleteFlow.js'
import { I18n } from '../i18n.js'

const PENDING_KEY = '__pendingAccountDelete'
const FILTER_KEY  = '__homeFilters'
const ACCOUNTS_URL = '/pages/lists/AccountList.html'

export function confirmAccountDelete(accountId, accountName, opts = {}) {
    const impact  = fetchDeleteImpact('accounts', [Number(accountId)])
    const message = buildDeleteMessage('accounts', { count: 1, name: accountName, impact })
    const count   = impact?.deletedTransactions ?? 0

    showConfirm(message, () => deleteAccount(accountId, opts), I18n.t('deleteConfirmTitle'), {
        confirmLabel: I18n.t('commonDelete'),
        extraAction: count === 0
            ? null
            : { label: I18n.t('accountDeleteViewTransactions'), onClick: () => goToTransactions(accountId, accountName, opts) }
    })
}

export function resumePendingAccountDelete() {
    const pending = readJson(PENDING_KEY)
    sessionStorage.removeItem(PENDING_KEY)
    if (!pending?.id) return

    showConfirmBar(I18n.t('accountDeleteBarMessage', { name: pending.name ?? '' }), () => deleteAccount(pending.id), {
        confirmLabel: I18n.t('commonDelete')
    })
}

function goToTransactions(accountId, accountName, opts) {
    opts.onLeave?.()

    const today       = new Date()
    const firstOfYear = new Date(today.getFullYear(), 0, 1)
    const toStr = d => new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().split('T')[0]

    sessionStorage.setItem(FILTER_KEY, JSON.stringify({
        account:   String(accountId),
        startDate: toStr(firstOfYear),
        endDate:   toStr(today),
    }))
    sessionStorage.setItem(PENDING_KEY, JSON.stringify({ id: accountId, name: accountName }))
    navigate('/pages/HomePage.html')
}

function deleteAccount(accountId, opts = {}) {
    $.ajax({
        url:   `/api/accounts/${accountId}`,
        type:  'DELETE',
        async: false,
        success: () => {
            opts.onLeave?.()
            clearAccountFilter()
            sessionStorage.removeItem(PENDING_KEY)
            navigateWithToast(ACCOUNTS_URL, I18n.t('accountDeletedSuccess'), 'success')
        },
        error: xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorDeletingAccount'), 'error')
    })
}

function clearAccountFilter() {
    const filters = readJson(FILTER_KEY)
    if (!filters?.account) return
    filters.account = ''
    sessionStorage.setItem(FILTER_KEY, JSON.stringify(filters))
}

function readJson(key) {
    try { return JSON.parse(sessionStorage.getItem(key)) }
    catch { return null }
}
