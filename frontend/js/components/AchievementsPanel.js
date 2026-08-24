import { doRequest } from '../../utils/FrontendFunctions.js'
import { I18n } from '../i18n.js'

const TIERS = ['gold', 'silver', 'bronze']

export class AchievementsPanel {
    static render(containerId, highlightType = null) {
        const grid = document.getElementById(containerId)
        if (!grid) return

        const achievements = doRequest('/api/achievements', 'GET')
        if (!achievements) return

        grid.innerHTML = ''

        for (const tier of TIERS) {
            const items = achievements.filter(a => a.tier === tier)
            if (!items.length) continue

            const section = document.getElementById('tpl-achievement-section').content.firstElementChild.cloneNode(true)
            const heading = section.querySelector('.achievement-tier-heading')
            heading.classList.add(`tier-${tier}`)
            heading.textContent = I18n.t(`tier_${tier}`)

            const row = section.querySelector('.achievement-row')
            for (const a of items) row.appendChild(buildCard(a))

            grid.appendChild(section)
        }

        if (!highlightType) return

        const card = grid.querySelector(`[data-type="${highlightType}"]`)
        if (!card) return
        card.scrollIntoView({ behavior: 'smooth', block: 'center' })
        card.classList.add('card-highlighted')
        card.addEventListener('animationend', () => card.classList.remove('card-highlighted'), { once: true })
    }
}

function buildCard(a) {
    const card = document.getElementById('tpl-achievement-card').content.firstElementChild.cloneNode(true)
    card.classList.add(a.earned ? 'earned' : 'locked', `tier-${a.tier}`)
    card.dataset.type = a.type
    card.title = a.earned
        ? I18n.t('achievementEarnedOn', { date: new Date(a.earnedAt).toLocaleDateString(localeStr()) })
        : I18n.t('achievementLocked')

    card.querySelector('.achievement-icon').innerHTML = `<i class="ph ${a.iconKey}"></i>`
    card.querySelector('.achievement-title').textContent = I18n.t(`achievement_${a.type}_title`)
    card.querySelector('.achievement-desc').textContent  = I18n.t(`achievement_${a.type}_desc`)
    return card
}

function localeStr() {
    return { pt: 'pt-BR', en: 'en-US', es: 'es-ES' }[I18n.getLanguage()] ?? 'pt-BR'
}
