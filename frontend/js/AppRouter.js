import { SidebarManager } from './components/SidebarManager.js'
import { showPendingToast } from '../utils/FrontendFunctions.js'
import { I18n } from './i18n.js'

const routes = {
    '/pages/HomePage.html':                      () => import('./HomePage.js'),
    '/pages/Transaction.html':                   () => import('./Transaction.js'),
    '/pages/Transfer.html':                      () => import('./Transfer.js'),
    '/pages/Account.html':                       () => import('./Account.js'),
    '/pages/AccountDashboard.html':              () => import('./AccountDashboard.js'),
    '/pages/Category.html':                      () => import('./Category.js'),
    '/pages/CategoryDashboard.html':             () => import('./CategoryDashboard.js'),
    '/pages/FinancialInstitution.html':          () => import('./FinancialInstitution.js'),
    '/pages/FinancialInstitutionDashboard.html': () => import('./FinancialInstitutionDashboard.js'),
    '/pages/TransactionLocale.html':             () => import('./TransactionLocale.js'),
    '/pages/TransactionLocaleDashboard.html':    () => import('./TransactionLocaleDashboard.js'),
    '/pages/User.html':                          () => import('./User.js'),
}

globalThis.__appRouter = { navigate }
await SidebarManager.initialize()

async function navigate(rawUrl) {
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

    // Resolve page title via data-i18n key when available
    const pageTitleEl = doc.querySelector('.topbar .page-title')
    const appTitleEl  = document.getElementById('page-title-text')
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

    history.pushState({ url: full }, '', full)

    SidebarManager.onNavigate()

    const loader = routes[path]
    if (loader) {
        const mod = await loader()
        mod.init?.()
    }

    showPendingToast()
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
    navigate(e.state?.url ?? location.pathname + location.search)
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
