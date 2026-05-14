import { doRequest, formatCurrency, navigate, showPendingToast } from '../utils/FrontendFunctions.js'
import { Account } from './class/AccountClass.js'
import { SidebarManager } from './components/SidebarManager.js'
import { I18n } from './i18n.js'

const CHART_CDN = 'https://cdn.jsdelivr.net/npm/chart.js@4/dist/chart.umd.min.js'
const DONUT_COLORS = [
    '#4CAF50', '#2563EB', '#DC2626', '#F59E0B', '#8B5CF6',
    '#06B6D4', '#EC4899', '#F97316', '#14B8A6', '#84CC16',
    '#6366F1', '#78716C',
]

let chartInstances = {}
let _themeObserver = null

// ── Canvas-reveal animation helpers ──────────────────────────────────────────
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

// Left-to-right rectangle reveal (bar chart & line chart)
const REVEAL_LR = createRevealPlugin('revealLR', (ctx, { left, right, top, bottom }, p) => {
    ctx.rect(left, top - 10, (right - left) * p, bottom - top + 20)
})

// Clockwise arc reveal from 12 o'clock (donut charts)
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

export async function init() {
    Object.keys(chartInstances).forEach(destroyChart)
    hideOthersLegendTip()
    document.body.classList.add('page-charts')
    SidebarManager.initialize()
    showPendingToast()
    Account.addAccounts('account-input')

    document.getElementById('period-input').addEventListener('change', loadAndRender)
    document.getElementById('account-input').addEventListener('change', loadAndRender)
    I18n.onChange(() => loadAndRender())

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

    updateStatCards(data)
    renderMonthlyChart(data.monthlyData)
    renderWealthChart(data.wealthEvolution)
    renderDonutChart('chart-cat-expenses', data.categoryExpenses)
    renderDonutChart('chart-cat-income', data.categoryIncomes)
}

function updateStatCards(data) {
    const totalIncome   = data.monthlyData.reduce((s, m) => s + (m.income   ?? 0), 0)
    const totalExpenses = data.monthlyData.reduce((s, m) => s + (m.expenses ?? 0), 0)
    const net           = totalIncome - totalExpenses
    const wealth        = data.wealthEvolution.at(-1)?.balance ?? 0

    setCard('stat-income',   totalIncome,   'positive')
    setCard('stat-expenses', totalExpenses, 'negative')
    setCard('stat-net',      net,           net >= 0 ? 'positive' : 'negative')
    setCard('stat-wealth',   wealth,        wealth >= 0 ? 'positive' : 'negative')
}

function setCard(id, value, cls) {
    const el = document.getElementById(id)
    if (!el) return
    el.textContent = `$ ${formatCurrency(value)}`
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
            ticks: { color: c.textSecondary, callback: v => `$ ${formatCurrency(v)}` },
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
                    legend: { labels: { color: c.text, boxRadius: 4 } },
                    tooltip: { callbacks: { label: ctx => ` $ ${formatCurrency(ctx.raw)}` } },
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
                    tooltip: { callbacks: { label: ctx => ` $ ${formatCurrency(ctx.raw)}` } },
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

// ── Legend "Others" tooltip helpers ──────────────────────────────────────────
function showOthersLegendTip(nativeEvent, details) {
    hideOthersLegendTip()
    const tip = document.createElement('div')
    tip.id = '__others-tip'
    tip.className = 'others-legend-tip'
    tip.innerHTML = details.map(d => `<div>• ${d.name}: $ ${formatCurrency(d.total)}</div>`).join('')
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

// ── Donut chart ───────────────────────────────────────────────────────────────
function renderDonutChart(canvasId, categoryData) {
    const hasData = categoryData?.length > 0
    setChartVisibility(canvasId, hasData)
    if (!hasData) { destroyChart(canvasId); return }

    const c = themeColors()

    const MAX = 9
    let labels, values, colors, categoryIds, othersDetails, othersIndex

    if (categoryData.length <= MAX) {
        labels       = categoryData.map(d => d.categoryName)
        values       = categoryData.map(d => d.total)
        colors       = categoryData.map((_, i) => DONUT_COLORS[i % DONUT_COLORS.length])
        categoryIds  = categoryData.map(d => d.categoryId)
        othersDetails = []
        othersIndex  = -1
    } else {
        const top     = categoryData.slice(0, MAX - 1)
        const rest    = categoryData.slice(MAX - 1)
        const othersTotal = rest.reduce((s, d) => s + d.total, 0)
        labels        = [...top.map(d => d.categoryName), I18n.t('others')]
        values        = [...top.map(d => d.total), othersTotal]
        colors        = [...top.map((_, i) => DONUT_COLORS[i]), '#9CA3AF']
        categoryIds   = [...top.map(d => d.categoryId), null]
        othersDetails = rest.map(d => ({ name: d.categoryName, total: d.total }))
        othersIndex   = labels.length - 1
    }

    const total = values.reduce((a, b) => a + b, 0)

    const existing = chartInstances[canvasId]
    if (existing) {
        existing.data.labels                      = labels
        existing.data.datasets[0].data            = values
        existing.data.datasets[0].backgroundColor = colors
        existing._categoryIds                     = categoryIds
        existing._othersDetails                   = othersDetails
        existing._othersIndex                             = othersIndex
        existing._revealProgress                          = 1
        existing.data.datasets[0].borderColor             = c.surface
        existing.options.plugins.legend.labels.color      = c.text
        existing.options.animation                        = { duration: 400, easing: 'easeOutQuart' }
        existing.update()
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
                    const catId = chartInstances[canvasId]?._categoryIds?.[elements[0].index]
                    if (catId == null) return
                    goToHomepageFiltered(catId)
                },
                plugins: {
                    legend: {
                        position: 'right',
                        labels: { color: c.text, boxRadius: 4, padding: 10, font: { size: 11 }, boxWidth: 12 },
                        onHover: (event, legendItem) => {
                            const inst = chartInstances[canvasId]
                            if (!inst || legendItem.index !== inst._othersIndex || !inst._othersDetails?.length) {
                                hideOthersLegendTip()
                                return
                            }
                            showOthersLegendTip(event.native, inst._othersDetails)
                        },
                        onLeave: () => hideOthersLegendTip(),
                    },
                    tooltip: {
                        callbacks: {
                            label: ctx => {
                                const pct = total > 0 ? Math.round((ctx.raw / total) * 100) : 0
                                return ` $ ${formatCurrency(ctx.raw)} (${pct}%)`
                            },
                            afterLabel: ctx => {
                                const inst = chartInstances[canvasId]
                                if (!inst?._othersDetails?.length || ctx.dataIndex !== inst._othersIndex) return []
                                return inst._othersDetails.map(d => `  • ${d.name}: $ ${formatCurrency(d.total)}`)
                            },
                        },
                    },
                },
            },
            plugins: [REVEAL_ARC],
        }
    )
    chartInstances[canvasId]._categoryIds   = categoryIds
    chartInstances[canvasId]._othersDetails = othersDetails
    chartInstances[canvasId]._othersIndex   = othersIndex
    startReveal(chartInstances[canvasId])
}

if (!globalThis.__appRouter) init()
