import { doRequest, showPendingToast } from '../utils/FrontendFunctions.js'
import { SidebarManager } from './components/SidebarManager.js'
import { I18n } from './i18n.js'

export function init() {
    document.body.classList.add('page-dashboard')
    SidebarManager.initialize()
    showPendingToast()
    loadAchievements()
    I18n.onChange(loadAchievements)
}

function loadAchievements() {
    const data = doRequest('/api/achievements', 'GET')
    if (!data) return
    renderGrid(data)
}

function renderGrid(achievements) {
    const grid = document.getElementById('achievements-grid')
    if (!grid) return
    grid.innerHTML = ''

    const tiers = ['gold', 'silver', 'bronze']
    for (const tier of tiers) {
        const items = achievements.filter(a => a.tier === tier)
        if (!items.length) continue

        const section = document.createElement('div')
        section.className = 'achievement-section'

        const heading = document.createElement('h2')
        heading.className = `achievement-tier-heading tier-${tier}`
        heading.textContent = I18n.t(`tier_${tier}`)
        section.appendChild(heading)

        const row = document.createElement('div')
        row.className = 'achievement-row'

        for (const a of items) {
            const card = document.createElement('div')
            card.className = `achievement-card ${a.earned ? 'earned' : 'locked'} tier-${a.tier}`
            card.dataset.type = a.type
            card.title = a.earned
                ? I18n.t('achievementEarnedOn', { date: new Date(a.earnedAt).toLocaleDateString(localeStr()) })
                : I18n.t('achievementLocked')

            const iconWrap = document.createElement('div')
            iconWrap.className = 'achievement-icon'
            iconWrap.innerHTML = `<i class="ph ${a.iconKey}"></i>`

            const title = document.createElement('div')
            title.className = 'achievement-title'
            title.textContent = I18n.t(`achievement_${a.type}_title`)

            const desc = document.createElement('div')
            desc.className = 'achievement-desc'
            desc.textContent = I18n.t(`achievement_${a.type}_desc`)

            card.appendChild(iconWrap)
            card.appendChild(title)
            card.appendChild(desc)
            row.appendChild(card)
        }

        section.appendChild(row)
        grid.appendChild(section)
    }

    const highlightType = new URLSearchParams(location.search).get('highlight')
    if (highlightType) {
        const card = grid.querySelector(`[data-type="${highlightType}"]`)
        if (card) {
            card.scrollIntoView({ behavior: 'smooth', block: 'center' })
            card.classList.add('card-highlighted')
            card.addEventListener('animationend', () => card.classList.remove('card-highlighted'), { once: true })
        }
    }
}

function localeStr() {
    return { pt: 'pt-BR', en: 'en-US', es: 'es-ES' }[I18n.getLanguage()] ?? 'pt-BR'
}

if (!globalThis.__appRouter) init()
