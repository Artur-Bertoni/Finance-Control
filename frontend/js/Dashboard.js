import { doRequest, formatMoney, navigate, showPendingToast, initFilterToggle } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { createModal } from './components/Modal.js'
import { MascotManager } from './components/MascotManager.js'
import { I18n } from './i18n.js'
import { FinnySvg } from './utils/FinnySvg.js'
import { UserSettings } from './utils/UserSettings.js'

const CHART_CDN = '/vendor/chart.umd.min.js'
const FILTERS_KEY = '__dashboardFilters'
const DONUT_COLORS = [
    '#4CAF50', '#2563EB', '#DC2626', '#F59E0B', '#8B5CF6',
    '#06B6D4', '#EC4899', '#F97316', '#14B8A6', '#84CC16',
    '#6366F1', '#78716C',
]

let chartInstances = {}
let _themeObserver = null
let filterToggle = null

function easeInOutSine(t) { return -(Math.cos(Math.PI * t) - 1) / 2 }

function createRevealPlugin(id, buildPath) {
    return {
        id,
        beforeInit:         (chart) => { chart._revealProgress = 0 },
        beforeDatasetsDraw: (chart) => {
            const p = chart._revealProgress
            if (p >= 1) return
            const { ctx, chartArea } = chart
            if (!chartArea) return
            ctx.save()
            ctx.beginPath()
            buildPath(ctx, chartArea, p)
            ctx.clip()
        },
        afterDatasetsDraw: (chart) => {
            if (chart._revealProgress < 1) chart.ctx.restore()
        },
    }
}

const REVEAL_LR = createRevealPlugin('revealLR', (ctx, { left, right, top, bottom }, p) => {
    ctx.rect(left, top - 10, (right - left) * p, bottom - top + 20)
})

const REVEAL_ARC = createRevealPlugin('revealArc', (ctx, { left, right, top, bottom }, p) => {
    const cx = (left + right) / 2
    const cy = (top + bottom) / 2
    const r  = Math.hypot(right - left, bottom - top) / 2 + 10
    ctx.moveTo(cx, cy)
    ctx.arc(cx, cy, r, -Math.PI / 2, -Math.PI / 2 + 2 * Math.PI * p)
    ctx.closePath()
})

function startReveal(chart) {
    const start = performance.now()
    function frame(now) {
        if (!chart.canvas) return
        const t = Math.min((now - start) / 3000, 1)
        chart._revealProgress = easeInOutSine(t)
        chart.draw()
        if (t < 1) requestAnimationFrame(frame)
        else chart._revealProgress = 1
    }
    requestAnimationFrame(frame)
}

function saveFilters() {
    const period  = document.getElementById('period-input')?.value  ?? '1m'
    const account = document.getElementById('account-input')?.value ?? ''
    localStorage.setItem(FILTERS_KEY, JSON.stringify({ period, account }))
}

function restoreFilters() {
    try {
        const saved = JSON.parse(localStorage.getItem(FILTERS_KEY) ?? 'null')
        if (!saved) return
        const periodEl  = document.getElementById('period-input')
        const accountEl = document.getElementById('account-input')
        if (periodEl  && saved.period  !== undefined) periodEl.value  = saved.period
        if (accountEl && saved.account !== undefined) accountEl.value = saved.account
    } catch { }
}

function isFilterActive() {
    const period  = document.getElementById('period-input')?.value  ?? '1m'
    const account = document.getElementById('account-input')?.value ?? ''
    return !(period === '1m' && account === '')
}

function syncClearBtn() {
    const btn = document.getElementById('clear-filters-btn')
    if (!btn) return
    btn.style.display = isFilterActive() ? '' : 'none'
    filterToggle?.syncActive()
}

function clearFilters() {
    localStorage.removeItem(FILTERS_KEY)
    const periodEl  = document.getElementById('period-input')
    const accountEl = document.getElementById('account-input')
    if (periodEl)  periodEl.value  = '1m'
    if (accountEl) accountEl.value = ''
    syncClearBtn()
    loadAndRender()
}

function onFilterChange() {
    saveFilters()
    syncClearBtn()
    loadAndRender()
}

export async function init() {
    FinnySvg.autoInit()
    Object.keys(chartInstances).forEach(destroyChart)
    hideOthersLegendTip()
    document.body.classList.add('page-charts')
    SidebarManager.initialize()
    showPendingToast()
    Account.addAccounts('account-input')

    document.getElementById('period-input').addEventListener('change', onFilterChange)
    document.getElementById('account-input').addEventListener('change', onFilterChange)
    document.getElementById('clear-filters-btn')?.addEventListener('click', clearFilters)
    I18n.onChange(() => loadAndRender())

    restoreFilters()
    filterToggle = initFilterToggle(isFilterActive)
    syncClearBtn()

    _themeObserver?.disconnect()
    _themeObserver = new MutationObserver(loadAndRender)
    _themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })

    await loadChartJs()
    loadAndRender()
}

function loadChartJs() {
    return new Promise((resolve, reject) => {
        if (globalThis.Chart) { resolve(); return }
        if (document.querySelector(`script[src="${CHART_CDN}"]`)) {
            const wait = () => globalThis.Chart ? resolve() : setTimeout(wait, 50)
            wait()
            return
        }
        const s = document.createElement('script')
        s.src = CHART_CDN
        s.onload = resolve
        s.onerror = reject
        document.head.appendChild(s)
    })
}

function getPeriodDates() {
    const period = document.getElementById('period-input')?.value ?? '6m'
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

function loadAndRender() {
    if (!globalThis.Chart || !document.getElementById('chart-monthly')) return

    const { startDate, endDate } = getPeriodDates()
    const accountId = document.getElementById('account-input')?.value ?? ''

    const params = new URLSearchParams({ startDate, endDate })
    if (accountId) params.append('accountId', accountId)

    const data = doRequest(`/api/reports/dashboard?${params}`, 'GET')
    if (!data) return

    const goals = UserSettings.goals
        ? (() => { try { return doRequest('/api/goals', 'GET') ?? [] } catch { return [] } })()
        : []

    updateStatCards(data)
    renderMonthlyChart(data.monthlyData)
    renderWealthChart(data.balanceEvolution)
    renderDonutChart(DONUT_CANVAS.expense, data.categoryExpenses)
    renderDonutChart(DONUT_CANVAS.income, data.categoryIncomes)
    MascotManager.refreshFloatingTips(data, goals)
}

function updateStatCards(data) {
    const totalIncome   = data.monthlyData.reduce((s, m) => s + (m.income   ?? 0), 0)
    const totalExpenses = data.monthlyData.reduce((s, m) => s + (m.expenses ?? 0), 0)
    const net           = totalIncome - totalExpenses
    const wealth        = data.balanceEvolution.at(-1)?.balance ?? 0

    setCard('stat-income',   totalIncome,   'positive')
    setCard('stat-expenses', totalExpenses, 'negative')
    setCard('stat-net',      net,           net >= 0 ? 'positive' : 'negative')
    setCard('stat-wealth',   wealth,        wealth >= 0 ? 'positive' : 'negative')
}

function setCard(id, value, cls) {
    const el = document.getElementById(id)
    if (!el) return
    el.textContent = `${formatMoney(value)}`
    el.className = `stat-card-value ${cls}`
}

function monthLabel(key) {
    const [year, month] = key.split('-')
    const d = new Date(Number(year), Number(month) - 1, 1)
    const localeMap = { pt: 'pt-BR', en: 'en-US', es: 'es-ES' }
    const locale = localeMap[I18n.getLanguage()] ?? 'pt-BR'
    const mon = d.toLocaleDateString(locale, { month: 'short' }).replace('.', '')
    return `${mon}/${String(year).slice(2)}`
}

function themeColors() {
    const dark = document.documentElement.dataset.theme === 'dark'
    return {
        text:          dark ? '#F0F0F0' : '#111827',
        textSecondary: dark ? '#ABABAB' : '#4B5563',
        border:        dark ? 'rgba(255,255,255,.07)' : '#E5E7EB',
        surface:       dark ? '#262626' : '#FFFFFF',
        income:        '#16A34A',
        expenses:      '#DC2626',
        wealth:        '#15803D',
        wealthFill:    dark ? 'rgba(21,128,61,.22)' : 'rgba(21,128,61,.10)',
    }
}

function commonScaleOptions(c) {
    return {
        x: { ticks: { color: c.textSecondary }, grid: { color: c.border } },
        y: {
            ticks: { color: c.textSecondary, callback: v => `${formatMoney(v)}` },
            grid:  { color: c.border },
        },
    }
}

function destroyChart(id) {
    chartInstances[id]?.destroy()
    delete chartInstances[id]
}

function setChartVisibility(canvasId, hasData) {
    const canvas = document.getElementById(canvasId)
    const empty  = document.getElementById(`${canvasId}-empty`)
    if (canvas) canvas.style.display = hasData ? '' : 'none'
    if (empty)  empty.style.display  = hasData ? 'none' : 'flex'
}

function renderMonthlyChart(monthlyData) {
    const hasData = monthlyData?.some(m => m.income > 0 || m.expenses > 0)
    setChartVisibility('chart-monthly', hasData)
    if (!hasData) { destroyChart('chart-monthly'); return }

    const c      = themeColors()
    const labels = monthlyData.map(m => monthLabel(m.month))

    const existing = chartInstances['chart-monthly']
    if (existing) {
        existing._revealProgress                             = 1
        existing.data.labels                                 = labels
        existing.data.datasets[0].label                      = I18n.t('income')
        existing.data.datasets[0].data                       = monthlyData.map(m => m.income ?? 0)
        existing.data.datasets[1].label                      = I18n.t('expenses')
        existing.data.datasets[1].data                       = monthlyData.map(m => m.expenses ?? 0)
        existing.options.plugins.legend.labels.color         = c.text
        existing.options.scales.x.ticks.color                = c.textSecondary
        existing.options.scales.x.grid.color                 = c.border
        existing.options.scales.y.ticks.color                = c.textSecondary
        existing.options.scales.y.grid.color                 = c.border
        existing.options.animation                           = { duration: 400, easing: 'easeOutQuart' }
        existing.update()
        return
    }

    chartInstances['chart-monthly'] = new globalThis.Chart(
        document.getElementById('chart-monthly'),
        {
            type: 'bar',
            data: {
                labels,
                datasets: [
                    { label: I18n.t('income'),   data: monthlyData.map(m => m.income   ?? 0), backgroundColor: c.income,   borderRadius: 4, borderSkipped: false },
                    { label: I18n.t('expenses'),  data: monthlyData.map(m => m.expenses ?? 0), backgroundColor: c.expenses, borderRadius: 4, borderSkipped: false },
                ],
            },
            options: {
                animation: false,
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        labels: { color: c.text, boxRadius: 4 },
                        onHover: (_, legendItem) => {
                            const chart = chartInstances['chart-monthly']
                            if (!chart) return
                            const col = themeColors()
                            const full = [col.income, col.expenses]
                            chart.data.datasets.forEach((ds, i) => {
                                ds.backgroundColor = i === legendItem.datasetIndex ? full[i] : full[i] + '55'
                            })
                            chart.update('none')
                        },
                        onLeave: () => {
                            const chart = chartInstances['chart-monthly']
                            if (!chart) return
                            const col = themeColors()
                            chart.data.datasets[0].backgroundColor = col.income
                            chart.data.datasets[1].backgroundColor = col.expenses
                            chart.update('none')
                        },
                    },
                    tooltip: { callbacks: { label: ctx => ` ${formatMoney(ctx.raw)}` } },
                },
                scales: commonScaleOptions(c),
            },
            plugins: [REVEAL_LR],
        }
    )
    startReveal(chartInstances['chart-monthly'])
}

function renderWealthChart(wealthData) {
    const hasData = wealthData?.length > 0
    setChartVisibility('chart-wealth', hasData)
    if (!hasData) { destroyChart('chart-wealth'); return }

    const c      = themeColors()
    const labels = wealthData.map(m => monthLabel(m.month))
    const values = wealthData.map(m => m.balance ?? 0)

    const existing = chartInstances['chart-wealth']
    if (existing) {
        existing._revealProgress                             = 1
        existing.data.labels                                 = labels
        existing.data.datasets[0].label                      = I18n.t('patrimony')
        existing.data.datasets[0].data                       = values
        existing.data.datasets[0].borderColor                = c.wealth
        existing.data.datasets[0].backgroundColor            = c.wealthFill
        existing.options.plugins.legend.labels.color         = c.text
        existing.options.scales.x.ticks.color                = c.textSecondary
        existing.options.scales.x.grid.color                 = c.border
        existing.options.scales.y.ticks.color                = c.textSecondary
        existing.options.scales.y.grid.color                 = c.border
        existing.options.animation                           = { duration: 400, easing: 'easeOutQuart' }
        existing.update()
        return
    }

    chartInstances['chart-wealth'] = new globalThis.Chart(
        document.getElementById('chart-wealth'),
        {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: I18n.t('patrimony'),
                    data: values,
                    borderColor: c.wealth,
                    backgroundColor: c.wealthFill,
                    fill: true,
                    tension: 0.35,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                }],
            },
            options: {
                animation: false,
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { labels: { color: c.text, boxRadius: 4 } },
                    tooltip: { callbacks: { label: ctx => ` ${formatMoney(ctx.raw)}` } },
                },
                scales: commonScaleOptions(c),
            },
            plugins: [REVEAL_LR],
        }
    )
    startReveal(chartInstances['chart-wealth'])
}

function goToHomepageFiltered(categoryId) {
    const { startDate, endDate } = getPeriodDates()
    const accountId = document.getElementById('account-input')?.value ?? ''
    sessionStorage.setItem('__homeFilters', JSON.stringify({
        startDate,
        endDate,
        category: String(categoryId),
        account:  accountId,
    }))
    navigate('/pages/HomePage.html')
}

function showOthersLegendTip(nativeEvent, details) {
    hideOthersLegendTip()
    const tip = document.createElement('div')
    tip.id = '__others-tip'
    tip.className = 'others-legend-tip'
    for (const d of details) {
        const row = document.createElement('div')
        row.textContent = `• ${d.name}: ${formatMoney(d.total)}`
        tip.appendChild(row)
    }
    document.body.appendChild(tip)

    const x = nativeEvent.clientX + 14
    const y = nativeEvent.clientY - 10
    const w = tip.offsetWidth  || 200
    const h = tip.offsetHeight || 80
    tip.style.left = `${Math.min(x, window.innerWidth  - w - 12)}px`
    tip.style.top  = `${Math.max(10, Math.min(y, window.innerHeight - h - 12))}px`
}

function hideOthersLegendTip() {
    document.getElementById('__others-tip')?.remove()
}

const donutHidden = {}
const donutData = {}
const DONUT_TOGGLE_MS = 420
const DONUT_MAX_SLICES = 9
const OTHERS_COLOR = '#9CA3AF'

const DONUT_CANVAS = { expense: 'chart-cat-expenses', income: 'chart-cat-income' }

function donutKind(canvasId) {
    return canvasId === DONUT_CANVAS.income ? 'income' : 'expense'
}

function chartCategoryConfig(canvasId) {
    const settings = globalThis.__currentUser?.settings ?? {}
    const kind     = donutKind(canvasId)
    const pinned   = kind === 'income' ? settings.chartIncomePinnedCategories  : settings.chartExpensePinnedCategories
    const grouped  = kind === 'income' ? settings.chartIncomeGroupedCategories : settings.chartExpenseGroupedCategories
    return { pinned: new Set(pinned ?? []), grouped: new Set(grouped ?? []) }
}

function donutKey(categoryIds, othersIndex, i) {
    const catId = categoryIds[i]
    if (catId != null) return `c${catId}`
    return i === othersIndex ? '__others__' : `i${i}`
}

function isDonutHidden(canvasId, categoryIds, othersIndex, i) {
    return donutHidden[canvasId]?.has(donutKey(categoryIds, othersIndex, i)) ?? false
}

function donutVisibleTotal(chart) {
    const values = chart?._realValues ?? chart?.data?.datasets?.[0]?.data ?? []
    return values.reduce((sum, v, i) => sum + (chart.getDataVisibility(i) ? v : 0), 0)
}

function cancelDonutSliceAnims(chart) {
    chart?._sliceAnims?.forEach(cancel => cancel())
    chart?._sliceAnims?.clear()
}

function animateDonutSlice(canvasId, index, hide) {
    const inst = chartInstances[canvasId]
    if (!inst) return
    const dataset = inst.data.datasets[0]
    const real    = inst._realValues?.[index] ?? dataset.data[index]
    const anims   = (inst._sliceAnims ??= new Map())

    const running = anims.has(index)
    anims.get(index)?.()
    let cancelled = false
    anims.set(index, () => { cancelled = true })

    if (!inst.getDataVisibility(index)) inst.toggleDataVisibility(index)

    const from  = running ? dataset.data[index] : (hide ? real : 0)
    const to    = hide ? 0 : real
    const start = performance.now()

    function frame(now) {
        if (cancelled || !inst.canvas) return
        const t = Math.min((now - start) / DONUT_TOGGLE_MS, 1)
        dataset.data[index] = from + (to - from) * easeInOutSine(t)
        inst.update('none')
        if (t < 1) { requestAnimationFrame(frame); return }
        anims.delete(index)
        dataset.data[index] = real
        if (hide) inst.toggleDataVisibility(index)
        inst.update('none')
    }

    dataset.data[index] = from
    inst.update('none')
    requestAnimationFrame(frame)
}

function applyDonutVisibility(canvasId, categoryIds, othersIndex) {
    const inst = chartInstances[canvasId]
    if (!inst) return
    cancelDonutSliceAnims(inst)
    const hidden = donutHidden[canvasId]
    const keys = categoryIds.map((_, i) => donutKey(categoryIds, othersIndex, i))
    if (hidden) {
        hidden.forEach(k => { if (!keys.includes(k)) hidden.delete(k) })
    }
    keys.forEach((k, i) => {
        const visible = !hidden?.has(k)
        if (inst.getDataVisibility(i) !== visible) inst.toggleDataVisibility(i)
    })
    inst.update('none')
}

function splitDonutData(categoryData, canvasId) {
    const { pinned, grouped } = chartCategoryConfig(canvasId)
    const forcedIn = [], automatic = [], forcedOut = []

    for (const d of categoryData) {
        if      (grouped.has(d.categoryId)) forcedOut.push(d)
        else if (pinned.has(d.categoryId))  forcedIn.push(d)
        else                                automatic.push(d)
    }

    const needsOthers = forcedOut.length > 0 || forcedIn.length + automatic.length > DONUT_MAX_SLICES
    const slots       = needsOthers ? Math.max(0, DONUT_MAX_SLICES - 1 - forcedIn.length) : automatic.length
    const byTotalDesc = (a, b) => b.total - a.total

    return {
        separate: [...forcedIn, ...automatic.slice(0, slots)].sort(byTotalDesc),
        others:   [...forcedOut, ...automatic.slice(slots)].sort(byTotalDesc),
    }
}

function renderDonutChart(canvasId, categoryData) {
    const hasData = categoryData?.length > 0
    donutData[canvasId] = categoryData ?? []
    setChartVisibility(canvasId, hasData)
    if (!hasData) { destroyChart(canvasId); return }

    const c = themeColors()

    const { separate, others } = splitDonutData(categoryData, canvasId)

    const othersDetails = others.map(d => ({
        categoryId: d.categoryId,
        name:       d.categoryName,
        iconKey:    d.iconKey ?? null,
        total:      d.total,
    }))

    const slices = separate.map(d => ({
        label:      d.categoryName,
        value:      d.total,
        categoryId: d.categoryId,
        iconKey:    d.iconKey ?? null,
        isOthers:   false,
    }))

    if (othersDetails.length) {
        slices.push({
            label:      I18n.t('others'),
            value:      others.reduce((sum, d) => sum + d.total, 0),
            categoryId: null,
            iconKey:    null,
            isOthers:   true,
        })
        slices.sort((a, b) => b.value - a.value)
    }

    let paletteIndex = 0
    const labels      = slices.map(s => s.label)
    const values      = slices.map(s => s.value)
    const colors      = slices.map(s => s.isOthers ? OTHERS_COLOR : DONUT_COLORS[paletteIndex++ % DONUT_COLORS.length])
    const categoryIds = slices.map(s => s.categoryId)
    const iconKeys    = slices.map(s => s.iconKey)
    const othersIndex = slices.findIndex(s => s.isOthers)

    const existing = chartInstances[canvasId]
    if (existing) {
        existing.data.labels                      = labels
        existing.data.datasets[0].data            = values
        existing.data.datasets[0].backgroundColor = colors
        existing._categoryIds                     = categoryIds
        existing._iconKeys                        = iconKeys
        existing._othersDetails                   = othersDetails
        existing._othersIndex                     = othersIndex
        existing._realValues                      = [...values]
        existing._revealProgress                  = 1
        existing.data.datasets[0].borderColor     = c.surface
        existing.update('none')
        applyDonutVisibility(canvasId, categoryIds, othersIndex)
        renderDonutLegend(canvasId, { labels, colors, iconKeys, categoryIds, othersDetails, othersIndex })
        return
    }

    chartInstances[canvasId] = new globalThis.Chart(
        document.getElementById(canvasId),
        {
            type: 'doughnut',
            data: {
                labels,
                datasets: [{
                    data:            values,
                    backgroundColor: colors,
                    borderWidth:     2,
                    borderColor:     c.surface,
                    hoverOffset:     14,
                }],
            },
            options: {
                animation:           false,
                responsive:          true,
                maintainAspectRatio: false,
                onHover: (event, elements) => {
                    event.native.target.style.cursor = elements.length ? 'pointer' : 'default'
                },
                onClick: (_, elements) => {
                    if (!elements.length) return
                    const inst  = chartInstances[canvasId]
                    const index = elements[0].index
                    if (index === inst?._othersIndex) { openOthersModal(inst._othersDetails); return }
                    const catId = inst?._categoryIds?.[index]
                    if (catId == null) return
                    goToHomepageFiltered(catId)
                },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: ctx => {
                                const visibleTotal = donutVisibleTotal(chartInstances[canvasId])
                                const pct = visibleTotal > 0 ? Math.round((ctx.raw / visibleTotal) * 100) : 0
                                return ` ${formatMoney(ctx.raw)} (${pct}%)`
                            },
                            afterLabel: ctx => {
                                const inst = chartInstances[canvasId]
                                if (!inst?._othersDetails?.length || ctx.dataIndex !== inst._othersIndex) return []
                                return inst._othersDetails.map(d => `  • ${d.name}: ${formatMoney(d.total)}`)
                            },
                        },
                    },
                },
            },
            plugins: [REVEAL_ARC],
        }
    )
    chartInstances[canvasId]._categoryIds   = categoryIds
    chartInstances[canvasId]._iconKeys      = iconKeys
    chartInstances[canvasId]._othersDetails = othersDetails
    chartInstances[canvasId]._othersIndex   = othersIndex
    chartInstances[canvasId]._realValues    = [...values]
    applyDonutVisibility(canvasId, categoryIds, othersIndex)
    startReveal(chartInstances[canvasId])
    renderDonutLegend(canvasId, { labels, colors, iconKeys, categoryIds, othersDetails, othersIndex })
}

function renderDonutLegend(canvasId, { labels, colors, iconKeys, categoryIds, othersDetails, othersIndex }) {
    const container = document.getElementById(`${canvasId}-legend`)
    if (!container) return

    container.innerHTML = ''
    container.appendChild(buildLegendToolbar(canvasId))

    const list = document.createElement('div')
    list.className = 'donut-legend-list'
    container.appendChild(list)

    const entries = []
    labels.forEach((label, i) => {
        const item = document.createElement('div')
        item.className = 'donut-legend-item'
        item.dataset.index = String(i)
        item.setAttribute('role', 'button')

        const swatch = document.createElement('span')
        swatch.className = 'donut-legend-swatch'
        swatch.style.background = colors[i]

        const iconEl = document.createElement('span')
        iconEl.className = 'donut-legend-icon'
        if (iconKeys[i]) {
            iconEl.innerHTML = `<i class="ph ${iconKeys[i]}"></i>`
        }

        const nameEl = document.createElement('span')
        nameEl.className = 'donut-legend-name'
        nameEl.textContent = label

        const pctEl = document.createElement('span')
        pctEl.className = 'donut-legend-pct'

        item.appendChild(swatch)
        if (iconKeys[i]) item.appendChild(iconEl)
        item.appendChild(nameEl)
        item.appendChild(pctEl)

        item.addEventListener('mouseenter', () => {
            const inst = chartInstances[canvasId]
            if (!inst) return
            if (!isDonutHidden(canvasId, categoryIds, othersIndex, i)) {
                inst.setActiveElements([{ datasetIndex: 0, index: i }])
                inst.update('none')
            }
            container.querySelectorAll('.donut-legend-item').forEach((el, j) => {
                el.classList.toggle('dimmed', j !== i)
            })
            if (i === othersIndex && othersDetails.length) {
                showOthersLegendTip({ clientX: item.getBoundingClientRect().right, clientY: item.getBoundingClientRect().top }, othersDetails)
            }
        })
        item.addEventListener('mouseleave', () => {
            const inst = chartInstances[canvasId]
            if (inst) { inst.setActiveElements([]); inst.update('none') }
            container.querySelectorAll('.donut-legend-item').forEach(el => el.classList.remove('dimmed'))
            hideOthersLegendTip()
        })
        item.addEventListener('click', () => {
            const inst = chartInstances[canvasId]
            if (!inst) return
            const key    = donutKey(categoryIds, othersIndex, i)
            const hidden = (donutHidden[canvasId] ??= new Set())
            const hide   = !hidden.has(key)
            if (hide) hidden.add(key)
            else hidden.delete(key)
            inst.setActiveElements([])
            hideOthersLegendTip()
            animateDonutSlice(canvasId, i, hide)
            syncLegendState()
        })

        entries.push({ item, pctEl })
        list.appendChild(item)
    })

    function syncLegendState() {
        const values = chartInstances[canvasId]?._realValues ?? []
        const off = i => isDonutHidden(canvasId, categoryIds, othersIndex, i)
        const visibleTotal = values.reduce((sum, v, i) => sum + (off(i) ? 0 : v), 0)
        entries.forEach(({ item, pctEl }, i) => {
            item.classList.toggle('legend-off', off(i))
            item.setAttribute('aria-pressed', String(off(i)))
            const pct = visibleTotal > 0 ? Math.round((values[i] ?? 0) / visibleTotal * 100) : 0
            pctEl.textContent = off(i) ? '—' : `${pct}%`
        })
    }

    syncLegendState()
}

function buildLegendToolbar(canvasId) {
    const toolbar = document.createElement('div')
    toolbar.className = 'donut-legend-toolbar'

    const btn = document.createElement('button')
    btn.type = 'button'
    btn.className = 'donut-legend-config-btn'
    btn.title = I18n.t('legendConfigTitle')
    btn.setAttribute('aria-label', I18n.t('legendConfigTitle'))
    btn.innerHTML = '<i class="ph ph-gear"></i>'
    btn.addEventListener('click', () => openLegendConfigModal(canvasId))

    toolbar.appendChild(btn)
    return toolbar
}

function categoryIcon(iconKey) {
    const el = document.createElement('span')
    el.className = 'category-chip-icon'
    el.innerHTML = `<i class="ph ${iconKey || 'ph-tag'}"></i>`
    return el
}

function openOthersModal(details) {
    if (!details?.length) return
    hideOthersLegendTip()

    const total = details.reduce((sum, d) => sum + d.total, 0)
    const list  = document.createElement('div')
    list.className = 'others-modal-list'

    for (const detail of details) {
        const row = document.createElement('button')
        row.type = 'button'
        row.className = 'others-modal-row'

        const name = document.createElement('span')
        name.className = 'others-modal-name'
        name.textContent = detail.name

        const value = document.createElement('span')
        value.className = 'others-modal-value'
        const pct = total > 0 ? Math.round(detail.total / total * 100) : 0
        value.textContent = `${formatMoney(detail.total)} · ${pct}%`

        row.append(categoryIcon(detail.iconKey), name, value)
        row.addEventListener('click', () => {
            close()
            goToHomepageFiltered(detail.categoryId)
        })
        list.appendChild(row)
    }

    const { close } = createModal({
        title:     I18n.t('others'),
        message:   I18n.t('othersModalHint'),
        body:      list,
        cardClass: 'modal-card--list',
        actions:   [{ label: I18n.t('commonClose'), variant: 'secondary' }],
    })
}

function openLegendConfigModal(canvasId) {
    const categoryData = donutData[canvasId] ?? []
    if (!categoryData.length) return

    const { separate, others } = splitDonutData(categoryData, canvasId)

    const board = document.createElement('div')
    board.className = 'legend-config-board'

    const columns = {
        separate: buildConfigColumn('legendConfigSeparate', 'ph-chart-pie-slice'),
        grouped:  buildConfigColumn('legendConfigGrouped',  'ph-stack'),
    }

    const moveChip = (chip, targetList) => {
        targetList.appendChild(chip)
        refreshColumnCounts()
    }

    const refreshColumnCounts = () => {
        for (const column of Object.values(columns)) {
            column.count.textContent = String(column.list.children.length)
            column.list.classList.toggle('is-empty', column.list.children.length === 0)
        }
    }

    const buildChip = (data, side) => {
        const chip = document.createElement('div')
        chip.className = 'category-chip'
        chip.draggable = true
        chip.dataset.categoryId = String(data.categoryId)

        const name = document.createElement('span')
        name.className = 'category-chip-name'
        name.textContent = data.categoryName

        const move = document.createElement('button')
        move.type = 'button'
        move.className = 'category-chip-move'
        move.title = I18n.t(side === 'separate' ? 'legendConfigGrouped' : 'legendConfigSeparate')
        move.innerHTML = `<i class="ph ${side === 'separate' ? 'ph-arrow-right' : 'ph-arrow-left'}"></i>`
        move.addEventListener('click', () => {
            const target = side === 'separate' ? columns.grouped.list : columns.separate.list
            moveChip(rebuildChip(chip, data, side === 'separate' ? 'grouped' : 'separate'), target)
        })

        chip.append(categoryIcon(data.iconKey), name, move)
        chip.addEventListener('dragstart', event => {
            chip.classList.add('dragging')
            event.dataTransfer.effectAllowed = 'move'
            event.dataTransfer.setData('text/plain', chip.dataset.categoryId)
        })
        chip.addEventListener('dragend', () => chip.classList.remove('dragging'))

        return chip
    }

    const rebuildChip = (chip, data, side) => {
        const replacement = buildChip(data, side)
        chip.remove()
        return replacement
    }

    const chipData = new Map(categoryData.map(d => [String(d.categoryId), d]))

    separate.forEach(d => columns.separate.list.appendChild(buildChip(d, 'separate')))
    others.forEach(d => columns.grouped.list.appendChild(buildChip(d, 'grouped')))
    refreshColumnCounts()

    for (const [side, column] of Object.entries(columns)) {
        column.list.addEventListener('dragover', event => {
            event.preventDefault()
            column.list.classList.add('drag-over')
        })
        column.list.addEventListener('dragleave', () => column.list.classList.remove('drag-over'))
        column.list.addEventListener('drop', event => {
            event.preventDefault()
            column.list.classList.remove('drag-over')
            const id = event.dataTransfer.getData('text/plain')
            const dragged = board.querySelector(`.category-chip[data-category-id="${id}"]`)
            const data = chipData.get(id)
            if (!dragged || !data || dragged.parentElement === column.list) return
            moveChip(rebuildChip(dragged, data, side), column.list)
        })
        board.appendChild(column.wrapper)
    }

    const idsOf = list => [...list.children].map(chip => Number(chip.dataset.categoryId))

    const otherCanvasId = canvasId === DONUT_CANVAS.income ? DONUT_CANVAS.expense : DONUT_CANVAS.income
    const importLabel   = I18n.t('legendConfigImport', {
        source: I18n.t(otherCanvasId === DONUT_CANVAS.income ? 'income' : 'expenses'),
    })

    const otherConfig = chartCategoryConfig(otherCanvasId)

    const importOtherConfig = () => {
        for (const [side, column] of Object.entries(columns)) {
            for (const chip of [...column.list.children]) {
                const id     = Number(chip.dataset.categoryId)
                const target = otherConfig.grouped.has(id) ? 'grouped' : otherConfig.pinned.has(id) ? 'separate' : side
                if (target === side) continue
                moveChip(rebuildChip(chip, chipData.get(chip.dataset.categoryId), target), columns[target].list)
            }
        }
    }

    createModal({
        title:     I18n.t('legendConfigTitle'),
        message:   I18n.t('legendConfigHint'),
        body:      board,
        cardClass: 'modal-card--list modal-card--wide',
        actions: [
            {
                label:    importLabel,
                variant:  'ghost',
                align:    'start',
                closes:   false,
                disabled: !otherConfig.pinned.size && !otherConfig.grouped.size,
                onClick:  importOtherConfig,
            },
            { label: I18n.t('commonCancel'), variant: 'secondary' },
            {
                label: I18n.t('commonSave'),
                variant: 'primary',
                onClick: () => saveLegendConfig(canvasId, idsOf(columns.separate.list), idsOf(columns.grouped.list)),
            },
        ],
    })
}

function buildConfigColumn(titleKey, iconKey) {
    const wrapper = document.createElement('div')
    wrapper.className = 'legend-config-col'

    const header = document.createElement('div')
    header.className = 'legend-config-col-header'
    header.innerHTML = `<i class="ph ${iconKey}"></i><span>${I18n.t(titleKey)}</span>`

    const count = document.createElement('span')
    count.className = 'legend-config-col-count'
    header.appendChild(count)

    const list = document.createElement('div')
    list.className = 'legend-config-list'

    wrapper.append(header, list)
    return { wrapper, list, count }
}

function saveLegendConfig(canvasId, separateIds, groupedIds) {
    const expense = chartCategoryConfig(DONUT_CANVAS.expense)
    const income  = chartCategoryConfig(DONUT_CANVAS.income)
    const edited  = donutKind(canvasId)

    const payload = {
        expensePinned:  edited === 'expense' ? separateIds : [...expense.pinned],
        expenseGrouped: edited === 'expense' ? groupedIds  : [...expense.grouped],
        incomePinned:   edited === 'income'  ? separateIds : [...income.pinned],
        incomeGrouped:  edited === 'income'  ? groupedIds  : [...income.grouped],
    }

    const settings = doRequest('/api/user-settings/chart-categories', 'PUT', payload)
    if (!settings) return

    UserSettings.store(settings)
    renderDonutChart(canvasId, donutData[canvasId])
}

if (!globalThis.__appRouter) await init()
