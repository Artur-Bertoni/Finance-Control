import { doRequest, showToast } from '../../utils/FrontendFunctions.js'
import { showConfirm } from './ConfirmModal.js'
import { createModal } from '../components/Modal.js'
import { CustomSelect } from '../components/CustomSelect.js'
import { IconPicker } from '../components/IconPicker.js'
import { createIconPickerField } from '../components/IconPickerField.js'
import { InputMasks } from '../utils/InputMasks.js'
import { I18n } from '../i18n.js'
import { UserSettings } from '../utils/UserSettings.js'

const BOOL_OPTIONS = [['true', 'commonYes'], ['false', 'commonNo']]

const SCHEMAS = {
    'transactions': [
        { key: 'accountId',           label: 'transactionAccount', kind: 'ref',  source: '/api/accounts' },
        { key: 'categoryId',          label: 'category',           kind: 'ref',  source: '/api/categories' },
        { key: 'transactionLocaleId', label: 'location',           kind: 'ref',  source: '/api/transaction-locales', clearable: true, setting: 'localesEnabled' },
        { key: 'transactionType',     label: 'transactionType',    kind: 'enum', options: [['DEBIT', 'debit'], ['CREDIT', 'credit']] },
        { key: 'date',                label: 'transactionDate',    kind: 'date' },
        { key: 'value',               label: 'transactionValue',   kind: 'money', placeholder: 'valueZero', hint: 'bulkEditInstallmentHint' },
        { key: 'obs',                 label: 'observations',       kind: 'textarea', placeholder: 'observationsPlaceholder', clearable: true },
    ],
    'categories': [
        { key: 'description', label: 'description',  kind: 'textarea', placeholder: 'categoryDescriptionPlaceholder', clearable: true },
        { key: 'iconKey',     label: 'categoryIcon', kind: 'icon' },
    ],
    'accounts': [
        { key: 'financialInstitutionId', label: 'financialInstitution', kind: 'ref', source: '/api/financial-institutions', clearable: true, setting: 'institutionsEnabled' },
        { key: 'accountType',            label: 'accountType',          kind: 'enum', options: [['CHECKING', 'accountTypeChecking'], ['CREDIT_CARD', 'accountTypeCreditCard']] },
        { key: 'contact',                label: 'contact',              kind: 'text', placeholder: 'contactPlaceholder', clearable: true },
        { key: 'description',            label: 'description',          kind: 'textarea', placeholder: 'descriptionPlaceholder', clearable: true },
        { key: 'iconKey',                label: 'categoryIcon',         kind: 'icon' },
    ],
    'financial-institutions': [
        { key: 'address', label: 'institutionAddress', kind: 'text', placeholder: 'institutionAddressPlaceholder', clearable: true },
        { key: 'contact', label: 'contact',            kind: 'text', placeholder: 'institutionContactPlaceholder', clearable: true },
        { key: 'iconKey', label: 'categoryIcon',       kind: 'icon' },
    ],
    'transaction-locales': [
        { key: 'address', label: 'localeAddress', kind: 'text', placeholder: 'localeAddressPlaceholder', clearable: true },
        { key: 'iconKey', label: 'categoryIcon',  kind: 'icon' },
    ],
    'goals': [
        { key: 'goalType',         label: 'goalType',         kind: 'enum', options: [['expense_limit', 'goalTypeExpenseLimit'], ['savings', 'goalTypeSavings'], ['income', 'goalTypeIncome']] },
        { key: 'targetAmount',     label: 'goalTargetAmount', kind: 'money', placeholder: 'valueZero' },
        { key: 'startDate',        label: 'goalStartDate',    kind: 'date' },
        { key: 'endDate',          label: 'goalEndDate',      kind: 'date', clearable: true },
        { key: 'notifyAt50',       label: 'goalNotify50',     kind: 'enum', options: BOOL_OPTIONS },
        { key: 'notifyAt75',       label: 'goalNotify75',     kind: 'enum', options: BOOL_OPTIONS },
        { key: 'notifyAt90',       label: 'goalNotify90',     kind: 'enum', options: BOOL_OPTIONS },
        { key: 'notifyOnComplete', label: 'goalNotifyOnComplete', kind: 'enum', options: BOOL_OPTIONS },
        { key: 'notifyOnDeadline', label: 'goalNotifyOnDeadline', kind: 'enum', options: BOOL_OPTIONS },
        { key: 'notifyOnExceed',   label: 'goalNotifyOnExceed',   kind: 'enum', options: BOOL_OPTIONS },
    ],
    'budgets': [
        { key: 'monthlyLimit', label: 'budgetMonthlyLimit', kind: 'money', placeholder: 'valueZero' },
    ],
}

function schemaFor(type) {
    return (SCHEMAS[type] ?? []).filter(f => !f.setting || UserSettings.isEnabled(f.setting))
}

export function hasBulkEditSchema(type) {
    return schemaFor(type).length > 0
}

export function openBulkEditModal({ type, ids, onDone }) {
    const schema = schemaFor(type)
    if (!schema.length) return

    const container = document.createElement('div')
    container.className = 'bulk-edit-fields'
    for (const field of schema) container.appendChild(buildField(field))

    let close = null
    const modal = createModal({
        id: 'bulk-edit-overlay',
        cardClass: 'modal-card--form',
        title: I18n.t('bulkEditTitle', { count: ids.length }),
        message: I18n.t('bulkEditHelp'),
        body: container,
        actions: [
            { id: 'bulk-edit-cancel', label: I18n.t('commonCancel') },
            { id: 'bulk-edit-submit', label: I18n.t('bulkEditSubmit'), variant: 'primary', closes: false,
              onClick: () => submit(type, ids, schema, close, onDone) },
        ],
    })
    close = modal.close
    modal.card.querySelector('.modal-message').classList.add('bulk-edit-help')

    CustomSelect.autoInit()
    if (schema.some(f => f.kind === 'icon')) IconPicker.init(key => IconPicker.setValue(key))
}

function buildField(field) {
    const wrapper = document.createElement('div')
    wrapper.className = 'bulk-edit-field'

    const toggle = document.createElement('label')
    toggle.className = 'checkbox-label bulk-edit-toggle'
    const check = document.createElement('input')
    check.type = 'checkbox'
    check.dataset.toggle = field.key
    toggle.appendChild(check)
    const labelText = document.createElement('span')
    labelText.textContent = I18n.t(field.label)
    toggle.appendChild(labelText)
    wrapper.appendChild(toggle)

    const control = buildControl(field)
    control.classList.add('bulk-edit-control')
    wrapper.appendChild(control)

    if (field.hint) {
        const hint = document.createElement('p')
        hint.className = 'bulk-edit-hint'
        hint.textContent = I18n.t(field.hint)
        wrapper.appendChild(hint)
    }

    const inputs = control.querySelectorAll('input, select, textarea, button')
    const sync = () => {
        wrapper.classList.toggle('bulk-edit-field--on', check.checked)
        inputs.forEach(el => { el.disabled = !check.checked })
        CustomSelect.syncAll()
    }
    check.addEventListener('change', sync)
    sync()

    return wrapper
}

function buildControl(field) {
    if (field.kind === 'icon') return createIconPickerField()

    const box = document.createElement('div')

    if (field.kind === 'ref' || field.kind === 'enum') {
        const select = document.createElement('select')
        select.dataset.value = field.key
        if (field.clearable) select.appendChild(option('', I18n.t('bulkEditNoValue')))

        const entries = field.kind === 'enum'
            ? field.options.map(([value, key]) => [value, I18n.t(key)])
            : (doRequest(field.source, 'GET') ?? []).map(item => [String(item.id), item.name])

        for (const [value, label] of entries) select.appendChild(option(value, label))
        box.appendChild(select)
        return box
    }

    if (field.kind === 'textarea') {
        const area = document.createElement('textarea')
        area.dataset.value = field.key
        if (field.placeholder) area.placeholder = I18n.t(field.placeholder)
        box.appendChild(area)
        return box
    }

    const input = document.createElement('input')
    input.dataset.value = field.key

    if (field.kind === 'date') {
        input.type = 'date'
    } else if (field.kind === 'money') {
        input.type = 'text'
        input.inputMode = 'decimal'
        InputMasks.money(input)
    } else {
        input.type = 'text'
    }

    if (field.placeholder) input.placeholder = I18n.t(field.placeholder)
    box.appendChild(input)
    return box
}

function option(value, label) {
    const opt = document.createElement('option')
    opt.value = value
    opt.textContent = label
    return opt
}

function collect(schema) {
    const fields = []
    const values = {}

    for (const field of schema) {
        const toggle = document.querySelector(`#bulk-edit-overlay [data-toggle="${field.key}"]`)
        if (!toggle?.checked) continue

        fields.push(field.key)
        values[field.key] = readValue(field)
    }
    return { fields, values }
}

function readValue(field) {
    if (field.kind === 'icon')
        return document.querySelector('#bulk-edit-overlay #icon-key-input')?.value || null

    const el = document.querySelector(`#bulk-edit-overlay [data-value="${field.key}"]`)
    const raw = el?.value ?? ''

    if (raw === '') return null
    if (field.kind === 'ref')    return Number(raw)
    if (field.kind === 'money')  return Number(raw)
    if (field.kind === 'enum' && (raw === 'true' || raw === 'false')) return raw === 'true'
    return raw
}

function submit(type, ids, schema, close, onDone) {
    const { fields, values } = collect(schema)

    if (fields.length === 0) {
        showToast(I18n.t('bulkEditNoFieldSelected'), 'warning')
        return
    }

    const labels = schema.filter(f => fields.includes(f.key)).map(f => I18n.t(f.label)).join(', ')
    const message = `${I18n.t('bulkEditConfirm', { count: ids.length, fields: labels })} ${I18n.t('bulkEditConfirmIrreversible')}`

    showConfirm(message, () => run(type, ids, fields, values, close, onDone), I18n.t('bulkEditConfirmTitle'), {
        confirmLabel: I18n.t('bulkEditSubmit')
    })
}

function run(type, ids, fields, values, close, onDone) {
    let result = null
    $.ajax({
        url:         '/api/bulk-edit',
        type:        'POST',
        async:       false,
        contentType: 'application/json',
        data:        JSON.stringify({ type, ids, fields, values }),
        success: response => { result = response },
        error:   xhr => showToast(xhr.responseJSON?.message ?? I18n.t('bulkEditError'), 'error')
    })
    if (!result) return

    close()
    if (result.edited > 0) showToast(I18n.t('bulkEditSuccess', { count: result.edited }), 'success')
    else                   showToast(I18n.t('bulkEditNone'), 'warning')

    onDone?.()
}
