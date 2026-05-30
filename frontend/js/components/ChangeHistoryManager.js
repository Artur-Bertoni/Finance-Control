import { I18n } from '../i18n.js'
import { formatCurrency, formatDateTime } from '../../utils/FrontendFunctions.js'

const MONEY_FIELDS   = new Set(['balance', 'value', 'targetAmount'])
const DATE_FIELDS    = new Set(['date', 'startDate', 'endDate'])
const BOOLEAN_FIELDS = new Set([
    'emailNotificationEnabled', 'goalEmailNotificationEnabled',
    'notifyAt50', 'notifyAt75', 'notifyAt90',
    'notifyOnComplete', 'notifyOnDeadline', 'notifyOnExceed'
])

const ENUM_I18N = {
    debit:         'debit',
    credit:        'credit',
    savings:       'goalTypeSavings',
    income:        'goalTypeIncome',
    expense_limit: 'goalTypeExpenseLimit',
    active:        'goalStatusActive',
    completed:     'goalStatusCompleted',
    archived:      'goalStatusArchived',
    expired:       'goalStatusExpired',
    pt:            'languagePt',
    en:            'languageEn',
    es:            'languageEs',
}

const FIELD_I18N = {
    name:                       'histFieldName',
    balance:                    'histFieldBalance',
    financialInstitution:       'histFieldFinancialInstitution',
    contact:                    'histFieldContact',
    description:                'histFieldDescription',
    iconKey:                    'histFieldIconKey',
    account:                    'histFieldAccount',
    category:                   'histFieldCategory',
    value:                      'histFieldValue',
    date:                       'histFieldDate',
    type:                       'histFieldType',
    installmentsNumber:         'histFieldInstallments',
    obs:                        'histFieldObs',
    transactionLocale:          'histFieldLocale',
    aliases:                    'histFieldAliases',
    username:                   'histFieldUsername',
    email:                      'histFieldEmail',
    emailNotificationEnabled:   'histFieldEmailNotif',
    emailNotificationDay:       'histFieldEmailNotifDay',
    goalEmailNotificationEnabled:'histFieldGoalEmailNotif',
    language:                   'histFieldLanguage',
    targetAmount:               'histFieldTargetAmount',
    startDate:                  'histFieldStartDate',
    endDate:                    'histFieldEndDate',
    status:                     'histFieldStatus',
    categories:                 'histFieldCategories',
    locales:                    'histFieldLocales',
    notifyOnComplete:           'histFieldNotifyOnComplete',
    notifyOnDeadline:           'histFieldNotifyOnDeadline',
    notifyOnExceed:             'histFieldNotifyOnExceed',
    address:                    'histFieldAddress',
}

function getFieldLabel(fieldName) {
    const pct = fieldName.match(/^notifyAt(\d+)$/)
    if (pct) return I18n.t('histFieldNotifyAtPercent', { percent: Number(pct[1]) })
    return I18n.t(FIELD_I18N[fieldName] ?? fieldName)
}

function formatValue(fieldName, raw) {
    if (raw === null || raw === undefined || raw === '') return `<em>${I18n.t('histEmpty')}</em>`

    if (MONEY_FIELDS.has(fieldName)) {
        const n = parseFloat(raw)
        return isNaN(n) ? raw : `$ ${formatCurrency(Math.abs(n))}`
    }
    if (DATE_FIELDS.has(fieldName)) {
        const parts = raw.split('-')
        return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : raw
    }
    if (BOOLEAN_FIELDS.has(fieldName)) {
        return I18n.t(raw === 'true' ? 'enabled' : 'disabled')
    }
    if (fieldName === 'emailNotificationDay') {
        const days = ['', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']
        const idx  = parseInt(raw, 10)
        return !isNaN(idx) && days[idx] ? I18n.t(days[idx]) : raw
    }
    if (fieldName === 'iconKey') {
        return `<i class="ph ${raw}" style="font-size:1.3em;vertical-align:middle"></i>`
    }
    const enumKey = ENUM_I18N[raw]
    if (enumKey) return I18n.t(enumKey)
    return raw
}


export const ChangeHistoryManager = {
    load(entityType, entityId) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url:   `/api/change-history/${entityType}/${entityId}`,
                type:  'GET',
                async: true,
                success: resolve,
                error:   reject,
            })
        })
    },

    render(groups, createdAt, containerId) {
        const container = document.getElementById(containerId)
        if (!container) return

        if ((!groups || groups.length === 0) && !createdAt) {
            container.innerHTML = `<p class="history-empty">${I18n.t('histNoHistory')}</p>`
            return
        }

        let html = '<div class="history-timeline">'

        if (groups) {
            groups.forEach(group => {
                const dateStr = formatDateTime(group.changedAt)

                if (group.creation) {
                    html += `
                    <div class="history-group history-group--creation">
                        <div class="history-group-header">
                            <span class="history-group-marker history-group-marker--creation"></span>
                            <span class="history-group-label history-group-label--creation">${I18n.t('histCreation')} &mdash; ${dateStr}</span>
                        </div>
                    </div>`
                } else if (group.passwordChange) {
                    html += `
                    <div class="history-group">
                        <div class="history-group-header">
                            <span class="history-group-marker"></span>
                            <span class="history-group-label">${I18n.t('histChange')} &mdash; ${dateStr}</span>
                        </div>
                        <ul class="history-changes">
                            <li class="history-change-item">
                                <span class="history-field-name">${I18n.t('histPasswordChanged')}</span>
                            </li>
                        </ul>
                    </div>`
                } else if (group.changes && group.changes.length > 0) {
                    html += `
                    <div class="history-group">
                        <div class="history-group-header">
                            <span class="history-group-marker"></span>
                            <span class="history-group-label">${I18n.t('histChange')} &mdash; ${dateStr}</span>
                        </div>
                        <ul class="history-changes">`

                    group.changes.forEach(ch => {
                        const label  = getFieldLabel(ch.fieldName)
                        const oldVal = formatValue(ch.fieldName, ch.oldValue)
                        const newVal = formatValue(ch.fieldName, ch.newValue)
                        html += `
                            <li class="history-change-item">
                                <span class="history-field-name">${label}:</span>
                                <span class="history-value-old">${oldVal}</span>
                                <span class="history-arrow">&rarr;</span>
                                <span class="history-value-new">${newVal}</span>
                            </li>`
                    })

                    html += `</ul></div>`
                }
            })
        }

        const hasCreationLog = groups?.some(g => g.creation)
        if (!hasCreationLog && createdAt) {
            html += `
            <div class="history-group history-group--creation">
                <div class="history-group-header">
                    <span class="history-group-marker history-group-marker--creation"></span>
                    <span class="history-group-label history-group-label--creation">${I18n.t('histCreation')} &mdash; ${formatDateTime(createdAt)}</span>
                </div>
            </div>`
        }

        html += '</div>'
        container.innerHTML = html
    },

    async loadAndRender(entityType, entityId, createdAt, containerId) {
        const container = document.getElementById(containerId)
        if (container) container.innerHTML = `<p class="history-empty">${I18n.t('histLoading')}</p>`
        try {
            const groups = await this.load(entityType, entityId)
            this.render(groups, createdAt, containerId)
            I18n.onChange(() => this.render(groups, createdAt, containerId))
        } catch {
            if (container) container.innerHTML = `<p class="history-empty">${I18n.t('histError')}</p>`
        }
    }
}
