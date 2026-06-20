import { I18n } from '../i18n.js'
import { formatMoney, formatDateTime } from '../../utils/FrontendFunctions.js'

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

function _clone(id) {
    return document.getElementById(id).content.firstElementChild.cloneNode(true)
}

function _setValue(span, fieldName, raw) {
    if (raw === null || raw === undefined || raw === '') {
        const em = document.createElement('em')
        em.textContent = I18n.t('histEmpty')
        span.appendChild(em)
        return
    }
    if (fieldName === 'iconKey') {
        const i = document.createElement('i')
        i.className = `ph ${raw}`
        i.style.fontSize = '1.3em'
        i.style.verticalAlign = 'middle'
        span.appendChild(i)
        return
    }

    let text = raw
    if (MONEY_FIELDS.has(fieldName)) {
        const n = parseFloat(raw)
        text = isNaN(n) ? raw : formatMoney(Math.abs(n))
    } else if (DATE_FIELDS.has(fieldName)) {
        const parts = raw.split('-')
        text = parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : raw
    } else if (BOOLEAN_FIELDS.has(fieldName)) {
        text = I18n.t(raw === 'true' ? 'enabled' : 'disabled')
    } else if (fieldName === 'emailNotificationDay') {
        const days = ['', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']
        const idx  = parseInt(raw, 10)
        text = !isNaN(idx) && days[idx] ? I18n.t(days[idx]) : raw
    } else if (ENUM_I18N[raw]) {
        text = I18n.t(ENUM_I18N[raw])
    }
    span.textContent = text
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
        container.innerHTML = ''

        if ((!groups || groups.length === 0) && !createdAt) {
            container.appendChild(_emptyMsg('histNoHistory'))
            return
        }

        const timeline = _clone('tpl-hist-timeline')

        if (groups) {
            for (const group of groups) {
                const el = _groupElement(group)
                if (el) timeline.appendChild(el)
            }
        }

        const hasCreationLog = groups?.some(g => g.creation)
        if (!hasCreationLog && createdAt) {
            timeline.appendChild(_creationGroup(`${I18n.t('histCreation')} - ${formatDateTime(createdAt)}`))
        }

        container.appendChild(timeline)
    },

    async loadAndRender(entityType, entityId, createdAt, containerId) {
        const container = document.getElementById(containerId)
        if (container) { container.innerHTML = ''; container.appendChild(_emptyMsg('histLoading')) }
        try {
            const groups = await this.load(entityType, entityId)
            this.render(groups, createdAt, containerId)
            I18n.onChange(() => this.render(groups, createdAt, containerId))
        } catch {
            if (container) { container.innerHTML = ''; container.appendChild(_emptyMsg('histError')) }
        }
    }
}

function _emptyMsg(i18nKey) {
    const p = _clone('tpl-hist-empty')
    p.textContent = I18n.t(i18nKey)
    return p
}

function _creationGroup(labelText) {
    const g = _clone('tpl-hist-creation')
    g.querySelector('.history-group-label').textContent = labelText
    return g
}

function _groupElement(group) {
    const dateStr = formatDateTime(group.changedAt)

    if (group.creation) {
        return _creationGroup(`${I18n.t('histCreation')} - ${dateStr}`)
    }
    if (group.passwordChange) {
        const g = _clone('tpl-hist-password')
        g.querySelector('.history-group-label').textContent = `${I18n.t('histChange')} - ${dateStr}`
        g.querySelector('.history-field-name').textContent = I18n.t('histPasswordChanged')
        return g
    }
    if (group.changes && group.changes.length > 0) {
        const g = _clone('tpl-hist-change-group')
        g.querySelector('.history-group-label').textContent = `${I18n.t('histChange')} - ${dateStr}`
        const ul = g.querySelector('.history-changes')
        for (const ch of group.changes) {
            const li = _clone('tpl-hist-change-item')
            li.querySelector('.history-field-name').textContent = `${getFieldLabel(ch.fieldName)}:`
            _setValue(li.querySelector('.history-value-old'), ch.fieldName, ch.oldValue)
            _setValue(li.querySelector('.history-value-new'), ch.fieldName, ch.newValue)
            ul.appendChild(li)
        }
        return g
    }
    return null
}
