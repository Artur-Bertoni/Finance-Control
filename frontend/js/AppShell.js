import { SidebarManager } from './components/SidebarManager.js'
import { MascotManager } from './components/MascotManager.js'
import { setBreadcrumb, showConfirmAsync, showPendingToast, showPendingNotifications, showToast } from '../utils/FrontendFunctions.js'
import { I18n } from './i18n.js'
import { FinnySvg } from './utils/FinnySvg.js'

const routes = {
    '/pages/Dashboard.html':                          () => import('./Dashboard.js'),
    '/pages/HomePage.html':                           () => import('./HomePage.js'),
    '/pages/StatementImport.html':                    () => import('./StatementImport.js'),
    '/pages/FinnyCenter.html':                        () => import('./NotificationCenter.js'),
    '/pages/crud/Account.html':                       () => import('./crud/Account.js'),
    '/pages/crud/Category.html':                      () => import('./crud/Category.js'),
    '/pages/crud/FinancialInstitution.html':          () => import('./crud/FinancialInstitution.js'),
    '/pages/crud/Goal.html':                          () => import('./crud/Goal.js'),
    '/pages/crud/Transaction.html':                   () => import('./crud/Transaction.js'),
    '/pages/crud/TransactionLocale.html':             () => import('./crud/TransactionLocale.js'),
    '/pages/crud/Transfer.html':                      () => import('./crud/Transfer.js'),
    '/pages/crud/User.html':                          () => import('./crud/User.js'),
    '/pages/lists/AccountList.html':                  () => import('./lists/AccountList.js'),
    '/pages/lists/AchievementList.html':              () => import('./lists/AchievementList.js'),
    '/pages/lists/CategoryList.html':                 () => import('./lists/CategoryList.js'),
    '/pages/lists/FinancialInstitutionList.html':     () => import('./lists/FinancialInstitutionList.js'),
    '/pages/lists/GoalList.html':                     () => import('./lists/GoalList.js'),
    '/pages/lists/TransactionLocaleList.html':        () => import('./lists/TransactionLocaleList.js'),
    '/pages/views/AccountView.html':                  () => import('./views/AccountView.js'),
    '/pages/views/CategoryView.html':                 () => import('./views/CategoryView.js'),
    '/pages/views/FinancialInstitutionView.html':     () => import('./views/FinancialInstitutionView.js'),
    '/pages/views/GoalView.html':                     () => import('./views/GoalView.js'),
    '/pages/views/TransactionLocaleView.html':        () => import('./views/TransactionLocaleView.js'),
    '/pages/views/TransactionView.html':              () => import('./views/TransactionView.js'),
    '/pages/views/UserView.html':                     () => import('./views/UserView.js'),
}

let currentSpaUrl = location.pathname + location.search

globalThis.__appRouter = { navigate }
await SidebarManager.initialize()

if (globalThis.__currentUser && !globalThis.__currentUser.emailVerified) {
    const justRegistered = sessionStorage.getItem('showEmailVerificationNotice')
    if (justRegistered) sessionStorage.removeItem('showEmailVerificationNotice')
    setTimeout(() => showToast(I18n.t('verifyEmailToast'), 'warning', null, { saveToHistory: false }), 1200)
}

MascotManager.initFloating()
FinnySvg.autoInit()

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
    } catch {
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

    if (!_fromPopstate) {
        const prev = new URL(currentSpaUrl, location.href)
        const leavingEditPage = prev.pathname.startsWith('/pages/crud/') && prev.searchParams.has('id')
        if (leavingEditPage) history.replaceState({ url: full }, '', full)
        else                 history.pushState({ url: full }, '', full)
    }
    currentSpaUrl = full
    updateBackButton(path)

    SidebarManager.onNavigate()
    MascotManager.refreshBadge()

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
