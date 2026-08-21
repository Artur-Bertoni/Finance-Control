import { showConfirm } from './ConfirmModal.js'
import { I18n } from '../i18n.js'

const ENTITY_KEYS = {
    'transactions':           'bulkEntityTransactions',
    'budgets':                'bulkEntityBudgets',
    'goals':                  'bulkEntityGoals',
    'categories':             'bulkEntityCategories',
    'accounts':               'bulkEntityAccounts',
    'financial-institutions': 'bulkEntityFinancialInstitutions',
    'transaction-locales':    'bulkEntityTransactionLocales',
}

export function fetchDeleteImpact(type, ids) {
    let result = null
    $.ajax({
        url:         '/api/bulk-delete/preview',
        type:        'POST',
        async:       false,
        contentType: 'application/json',
        data:        JSON.stringify({ type, ids }),
        success: response => { result = response },
        error:   () => { result = null }
    })
    return result
}

export function buildDeleteMessage(type, { count = 1, name = null, question = null, impact = null } = {}) {
    const entity = I18n.t(ENTITY_KEYS[type] ?? 'bulkEntityItems')

    let head = question
    if (!head) {
        head = count === 1 && name
            ? I18n.t('deleteConfirmQuestionOne', { name: escapeHtml(name) })
            : I18n.t('deleteConfirmQuestionMany', { count, entity })
    }

    return [head, ...impactSentences(type, count, impact), I18n.t('deleteImpactIrreversible')].join(' ')
}

export function confirmDelete({ type, id, name = null, question = null, onConfirm }) {
    const impact = fetchDeleteImpact(type, [Number(id)])
    const message = buildDeleteMessage(type, { count: 1, name, question, impact })

    showConfirm(message, onConfirm, I18n.t('deleteConfirmTitle'), { confirmLabel: I18n.t('commonDelete') })
}

function impactSentences(type, count, impact) {
    if (!impact) return []
    const sentences = []

    if (type === 'transactions' && impact.deletedTransactions > count)
        sentences.push(I18n.t('deleteImpactExpandedTransactions', { count: impact.deletedTransactions }))
    else if (type !== 'transactions' && impact.deletedTransactions > 0)
        sentences.push(I18n.t('deleteImpactDeletesTransactions', { count: impact.deletedTransactions }))

    if (impact.unlinkedTransactions > 0)
        sentences.push(I18n.t('deleteImpactUnlinksTransactions', { count: impact.unlinkedTransactions }))
    if (impact.unlinkedAccounts > 0)
        sentences.push(I18n.t('deleteImpactUnlinksAccounts', { count: impact.unlinkedAccounts }))

    return sentences
}

function escapeHtml(value) {
    const div = document.createElement('div')
    div.textContent = value ?? ''
    return div.innerHTML
}
