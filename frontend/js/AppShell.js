import { SidebarManager } from './components/SidebarManager.js'
import { setBreadcrumb, showConfirmAsync, showPendingToast, showPendingNotifications } from '../utils/FrontendFunctions.js'
import { I18n } from './i18n.js'

const routes = {
    '/pages/Dashboard.html':                         () => import('./Dashboard.js'),
    '/pages/HomePage.html':                          () => import('./HomePage.js'),
    '/pages/Transaction.html':                       () => import('./Transaction.js'),
    '/pages/TransactionView.html':                   () => import('./TransactionView.js'),
    '/pages/Transfer.html':                          () => import('./Transfer.js'),
    '/pages/StatementImport.html':                   () => import('./StatementImport.js'),
    '/pages/Account.html':                           () => import('./Account.js'),
    '/pages/AccountView.html':                       () => import('./AccountView.js'),
    '/pages/AccountDashboard.html':                  () => import('./AccountDashboard.js'),
    '/pages/Category.html':                          () => import('./Category.js'),
    '/pages/CategoryView.html':                      () => import('./CategoryView.js'),
    '/pages/CategoryDashboard.html':                 () => import('./CategoryDashboard.js'),
    '/pages/FinancialInstitution.html':              () => import('./FinancialInstitution.js'),
    '/pages/FinancialInstitutionView.html':          () => import('./FinancialInstitutionView.js'),
    '/pages/FinancialInstitutionDashboard.html':     () => import('./FinancialInstitutionDashboard.js'),
    '/pages/TransactionLocale.html':                 () => import('./TransactionLocale.js'),
    '/pages/TransactionLocaleView.html':             () => import('./TransactionLocaleView.js'),
    '/pages/TransactionLocaleDashboard.html':        () => import('./TransactionLocaleDashboard.js'),
    '/pages/User.html':                              () => import('./User.js'),
    '/pages/UserView.html':                          () => import('./UserView.js'),
    '/pages/GoalDashboard.html':                     () => import('./GoalDashboard.js'),
    '/pages/GoalView.html':                          () => import('./GoalView.js'),
    '/pages/Goal.html':                              () => import('./Goal.js'),
    '/pages/AchievementDashboard.html':              () => import('./AchievementDashboard.js'),
    '/pages/NotificationCenter.html':                () => import('./NotificationCenter.js'),
}

let currentSpaUrl = location.pathname + location.search

globalThis.__appRouter = { navigate }
await SidebarManager.initialize()

document.getElementById('back-btn')?.addEventListener('click', () => history.back())

function updateBackButton(path) {
    const btn = document.getElementById('back-btn')
    if (!btn) return
    const isHome = path === '/pages/HomePage.html' || path === '/'
    btn.style.display = isHome ? 'none' : ''
}

async function confirmLeave(fromPopstate) {
    if (!globalThis.__dirtyGuard?.()) return true
    const confirmed = await showConfirmAsync(
        I18n.t('unsavedChangesWarning'),
        I18n.t('unsavedChangesTitle')
    )
    if (!confirmed && fromPopstate) history.pushState({ url: currentSpaUrl }, '', currentSpaUrl)
    return confirmed
}

async function navigate(rawUrl, { _fromPopstate = false } = {}) {
    if (!await confirmLeave(_fromPopstate)) return
    globalThis.__dirtyGuard = null

    const url  = new URL(rawUrl, location.href)
    const path = url.pathname
    const full = url.pathname + url.search

    let html
    try {
        const res = await fetch(full)
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        html = await res.text()
    } catch (e) {
        console.error('Navigation failed:', e)
        return
    }

    const doc = new DOMParser().parseFromString(html, 'text/html')

    document.title = doc.title

    // Reset breadcrumb — clear both DOM and module state so stale crumbs don't re-render on language change
    setBreadcrumb([])
    const breadcrumbNav = document.getElementById('breadcrumb')
    const appTitleEl    = document.getElementById('page-title-text')
    if (breadcrumbNav && appTitleEl) {
        breadcrumbNav.innerHTML = ''
        breadcrumbNav.appendChild(appTitleEl)
    }

    const pageTitleEl = doc.querySelector('.topbar .page-title')
    if (appTitleEl) {
        const i18nKey = pageTitleEl?.dataset?.i18n
        if (i18nKey) {
            appTitleEl.textContent    = I18n.t(i18nKey)
            appTitleEl.dataset.i18n   = i18nKey
        } else {
            appTitleEl.textContent    = pageTitleEl?.textContent?.trim() ?? ''
            delete appTitleEl.dataset.i18n
        }
    }

    const srcActions = doc.getElementById('header-actions')
    document.getElementById('header-actions').innerHTML = srcActions?.innerHTML ?? ''

    const srcContent = doc.querySelector('main.page-content') || doc.querySelector('.page-content')
    document.getElementById('view').innerHTML = srcContent?.innerHTML ?? ''

    const newClasses = [...doc.body.classList].filter(c => c.startsWith('page-'))
    ;[...document.body.classList].filter(c => c.startsWith('page-')).forEach(c => document.body.classList.remove(c))
    newClasses.forEach(c => document.body.classList.add(c))

    if (!_fromPopstate) history.pushState({ url: full }, '', full)
    currentSpaUrl = full
    updateBackButton(path)

    SidebarManager.onNavigate()

    const loader = routes[path]
    if (loader) {
        const mod = await loader()
        mod.init?.()
    }

    showPendingToast()
    showPendingNotifications()
}

document.addEventListener('click', e => {
    const a = e.target.closest('a[href]')
    if (!a) return
    const href = a.getAttribute('href')
    if (!href || href.startsWith('#') || href.startsWith('javascript:')) return
    const url = new URL(href, location.href)
    if (url.origin !== location.origin) return
    if (url.pathname.includes('Login.html') || url.pathname.includes('AppShell.html')) return
    e.preventDefault()
    navigate(href)
}, true)

globalThis.addEventListener('popstate', e => {
    navigate(e.state?.url ?? location.pathname + location.search, { _fromPopstate: true })
})

const pendingUrl = sessionStorage.getItem('__spa_url')
if (pendingUrl) {
    sessionStorage.removeItem('__spa_url')
    await navigate(pendingUrl)
} else if (routes[location.pathname]) {
    await navigate(location.pathname + location.search)
} else {
    await navigate('/pages/HomePage.html')
}
