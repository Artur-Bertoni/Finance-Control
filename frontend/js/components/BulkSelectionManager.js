import { showToast } from '../../utils/FrontendFunctions.js'
import { showConfirm } from '../modals/ConfirmModal.js'
import { buildDeleteMessage, fetchDeleteImpact } from '../modals/DeleteFlow.js'
import { hasBulkEditSchema, openBulkEditModal } from '../modals/BulkEditModal.js'
import { createActionBar } from './ActionBar.js'
import { I18n } from '../i18n.js'

const BAR_ID = 'bulk-action-bar'

let currentInstance = null
let activeInstance  = null
let listenersBound  = false

export function initBulkSelection(config) {
    activeInstance?.exitMode()
    document.getElementById(BAR_ID)?.remove()
    bindGlobalListeners()

    currentInstance = new BulkSelection(config)
    return currentInstance
}

function bindGlobalListeners() {
    if (listenersBound) return
    listenersBound = true

    document.addEventListener('click', e => activeInstance?.handleItemEvent(e), true)
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') {
            if (!document.querySelector('.modal-overlay')) activeInstance?.exitMode()
            return
        }
        if (e.key === 'Enter' || e.key === ' ') activeInstance?.handleItemEvent(e)
    }, true)

    I18n.onChange(() => currentInstance?.syncLabels())
}

class BulkSelection {

    constructor({ type, listId, actionsHostId = 'header-actions', allIds = null, onChanged = null }) {
        this.type      = type
        this.listId    = listId
        this.hostId    = actionsHostId
        this.allIdsFn  = allIds
        this.onChanged = onChanged
        this.selected  = new Set()
        this.active    = false
        this.bar       = null
        this.observer  = null
        this.viewWatch = null

        this._renderToggleButton()
    }

    get list() {
        return document.getElementById(this.listId)
    }

    handleItemEvent(e) {
        if (!this.active) return
        const item = e.target.closest?.('[data-bulk-id]')
        if (!item || !this.list?.contains(item)) return
        e.preventDefault()
        e.stopPropagation()
        this._toggleItem(item)
    }

    toggleMode() {
        if (this.active) this.exitMode()
        else this.enterMode()
    }

    enterMode() {
        if (!this.list) return
        activeInstance?.exitMode()

        this.active = true
        this.selected.clear()
        this.list.classList.add('bulk-mode')
        this.toggleBtn?.classList.add('active')
        this._decorateItems()
        this._watchList()
        this._renderBar()
        this.syncLabels()

        activeInstance = this
    }

    exitMode() {
        this.active = false
        this.selected.clear()
        this.observer?.disconnect()
        this.viewWatch?.disconnect()
        this.observer  = null
        this.viewWatch = null
        this._undecorateItems()
        this.list?.classList.remove('bulk-mode')
        this.toggleBtn?.classList.remove('active')
        this.bar?.close()
        this.bar = null
        this.syncLabels()

        if (activeInstance === this) activeInstance = null
    }

    syncLabels() {
        const label = this.toggleBtn?.querySelector('.bulk-toggle-label')
        if (label) label.textContent = I18n.t(this.active ? 'bulkSelectCancel' : 'bulkSelect')
        this._syncBar()
    }

    _renderToggleButton() {
        const host = document.getElementById(this.hostId)
        if (!host) return

        const btn = document.createElement('button')
        btn.type      = 'button'
        btn.id        = 'bulk-toggle-btn'
        btn.className = 'btn btn-ghost btn-sm bulk-toggle-btn'
        btn.innerHTML = '<i class="ph ph-check-square"></i><span class="bulk-toggle-label"></span>'
        btn.addEventListener('click', () => this.toggleMode())

        host.prepend(btn)
        this.toggleBtn = btn
        this.syncLabels()
    }

    _watchList() {
        if (this.list) {
            this.observer = new MutationObserver(() => {
                if (!this.active) return
                this._decorateItems()
                this._syncBar()
            })
            this.observer.observe(this.list, { childList: true, subtree: true })
        }

        const view = document.getElementById('view')
        if (!view) return
        this.viewWatch = new MutationObserver(() => {
            if (this.active && !this.list) this.exitMode()
        })
        this.viewWatch.observe(view, { childList: true })
    }

    _decorateItems() {
        for (const item of this._items()) {
            if (!item.querySelector(':scope > .bulk-check')) {
                const check = document.createElement('span')
                check.className = 'bulk-check'
                item.prepend(check)
            }
            item.classList.add('bulk-selectable')
            item.setAttribute('role', 'checkbox')
            item.setAttribute('tabindex', '0')
            this._syncItemState(item)
        }
    }

    _undecorateItems() {
        for (const item of this._items()) {
            item.querySelector(':scope > .bulk-check')?.remove()
            item.classList.remove('bulk-selectable', 'bulk-selected')
            item.removeAttribute('role')
            item.removeAttribute('tabindex')
            item.removeAttribute('aria-checked')
        }
    }

    _items() {
        return this.list ? [...this.list.querySelectorAll('[data-bulk-id]')] : []
    }

    _toggleItem(item) {
        const id = item.dataset.bulkId
        if (this.selected.has(id)) this.selected.delete(id)
        else this.selected.add(id)
        this._syncItemState(item)
        this._syncBar()
    }

    _syncItemState(item) {
        const on = this.selected.has(item.dataset.bulkId)
        item.classList.toggle('bulk-selected', on)
        item.setAttribute('aria-checked', String(on))
    }

    _selectAll() {
        const ids = this.allIdsFn ? this.allIdsFn().map(String) : this._items().map(i => i.dataset.bulkId)
        for (const id of ids) this.selected.add(id)
        for (const item of this._items()) this._syncItemState(item)
        this._syncBar()
    }

    _clearSelection() {
        this.selected.clear()
        for (const item of this._items()) this._syncItemState(item)
        this._syncBar()
    }

    _renderBar() {
        this.bar = createActionBar({
            id: BAR_ID,
            barClass: 'bulk-action-bar',
            actions: [
                { id: 'bulk-select-all-btn', variant: 'ghost',     closes: false, onClick: () => this._onSelectAllClick() },
                { id: 'bulk-cancel-btn',     variant: 'secondary', closes: false, onClick: () => this.exitMode() },
                hasBulkEditSchema(this.type)
                    && { id: 'bulk-edit-btn', variant: 'primary', closes: false, onClick: () => this._openEdit() },
                { id: 'bulk-delete-btn',     variant: 'danger',    closes: false, onClick: () => this._confirmDelete() },
            ],
        })

        this._syncBar()
    }

    _onSelectAllClick() {
        if (this._allSelected()) this._clearSelection()
        else this._selectAll()
    }

    _allSelected() {
        const total = this.allIdsFn ? this.allIdsFn().length : this._items().length
        return total > 0 && this.selected.size >= total
    }

    _syncBar() {
        if (!this.bar) return
        const empty = this.selected.size === 0

        this.bar.setMessage(I18n.t('bulkSelectedCount', { count: this.selected.size }))
        this.bar.setAction('bulk-select-all-btn', { label: I18n.t(this._allSelected() ? 'bulkClearSelection' : 'bulkSelectAll') })
        this.bar.setAction('bulk-cancel-btn', { label: I18n.t('commonCancel') })
        this.bar.setAction('bulk-edit-btn', { label: I18n.t('bulkEditBtn'), disabled: empty })
        this.bar.setAction('bulk-delete-btn', { label: I18n.t('bulkDeleteBtn'), disabled: empty })
    }

    _openEdit() {
        const ids = [...this.selected].map(Number).filter(Number.isFinite)
        if (ids.length === 0) {
            showToast(I18n.t('bulkEditNothingSelected'), 'warning')
            return
        }

        openBulkEditModal({
            type: this.type,
            ids,
            onDone: () => { this.exitMode(); this.onChanged?.() },
        })
    }

    _confirmDelete() {
        const ids = [...this.selected].map(Number).filter(Number.isFinite)
        if (ids.length === 0) {
            showToast(I18n.t('bulkDeleteNothingSelected'), 'warning')
            return
        }

        const impact  = fetchDeleteImpact(this.type, ids)
        const count   = impact?.items ?? ids.length
        const message = buildDeleteMessage(this.type, { count, impact })

        showConfirm(message, () => this._runDelete(ids), I18n.t('deleteConfirmTitle'), {
            confirmLabel: I18n.t('commonDelete')
        })
    }

    _runDelete(ids) {
        const result = this._post('/api/bulk-delete', ids, true)
        if (!result) return

        this.exitMode()
        if (result.deleted > 0) showToast(I18n.t('bulkDeleteSuccess', { count: result.deleted }), 'success')
        else                    showToast(I18n.t('bulkDeleteNone'), 'warning')

        this.onChanged?.()
    }

    _post(url, ids, reportError = false) {
        let result = null
        $.ajax({
            url,
            type:        'POST',
            async:       false,
            contentType: 'application/json',
            data:        JSON.stringify({ type: this.type, ids }),
            success: response => { result = response },
            error:   xhr => {
                result = null
                if (reportError) showToast(xhr.responseJSON?.message ?? I18n.t('bulkDeleteError'), 'error')
            }
        })
        return result
    }
}
