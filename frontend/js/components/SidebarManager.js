/**
 * Gerenciador de Sidebar
 * Centraliza toda a lógica de navegação e interatividade da barra lateral
 * Substitui os scripts inline minificados
 */

import { Icons } from '../icons/IconLibrary.js'
import { ThemeManager } from '../ThemeManager.js'

export class SidebarManager {
    /**
     * Inicializa a sidebar com todos os comportamentos
     */
    static initialize() {
        SidebarManager.checkAuth()
        SidebarManager.renderIcons()
        SidebarManager.renderDataIcons()
        SidebarManager.setupActiveLink()
        SidebarManager.setupToggleButton()
        SidebarManager.setupOverlayDismiss()
        ThemeManager.initialize()
    }

    static checkAuth() {
        $.ajax({
            url: '/api/auth/me',
            type: 'GET',
            async: false,
            error: function (xhr) {
                if (xhr.status === 401) globalThis.location.href = '/pages/Login.html'
            }
        })
    }

    /**
     * Renderiza os ícones da sidebar a partir da biblioteca centralizada
     */
    static renderIcons() {
        const iconMap = {
            'HomePage.html': 'home',
            'Transaction.html': 'transaction',
            'Transfer.html': 'transfer',
            'AccountDashboard.html': 'accounts',
            'CategoryDashboard.html': 'categories',
            'FinancialInstitutionDashboard.html': 'institutions',
            'TransactionLocaleDashboard.html': 'locations',
            'User.html': 'profile'
        }

        document.querySelectorAll('.sidebar-nav .sidebar-link, .sidebar-footer .sidebar-link').forEach(link => {
            const href = link.getAttribute('href')?.split('/').pop()
            const iconName = href ? iconMap[href] : null
            if (!iconName) return

            const label = link.textContent.trim()
            link.innerHTML = `${Icons[iconName]()} ${label}`
        })

        const toggleButton = document.getElementById('sidebar-toggle-btn')
        if (toggleButton) {
            toggleButton.innerHTML = Icons.menu()
        }
    }

    /**
     * Renderiza ícones em elementos com atributo data-icon
     */
    static renderDataIcons() {
        document.querySelectorAll('[data-icon]').forEach(el => {
            const iconName = el.dataset.icon
            const icon = Icons[iconName]
            if (icon) el.innerHTML = icon()
        })
    }

    /**
     * Marca o link ativo baseado na URL atual
     */
    static setupActiveLink() {
        const links = document.querySelectorAll('.sidebar-link[href]')
        const currentPage = location.pathname.split('/').pop()

        links.forEach(link => {
            const href = link.href.split('/').pop()
            if (href === currentPage) {
                link.classList.add('active')
            }
        })
    }

    /**
     * Configura o botão de toggle da sidebar para mobile
     */
    static setupToggleButton() {
        const toggleButton = document.getElementById('sidebar-toggle-btn')
        const sidebar = document.getElementById('sidebar')
        const overlay = document.getElementById('sidebar-overlay')

        if (!toggleButton || !sidebar) {
            console.warn('Sidebar toggle button ou sidebar element não encontrados')
            return
        }

        toggleButton.addEventListener('click', () => {
            sidebar.classList.toggle('open')
            if (overlay) {
                overlay.classList.toggle('show')
            }
        })
    }

    /**
     * Permite fechar a sidebar ao clicar no overlay
     */
    static setupOverlayDismiss() {
        const overlay = document.getElementById('sidebar-overlay')
        const sidebar = document.getElementById('sidebar')

        if (!overlay) return

        overlay.addEventListener('click', () => {
            sidebar?.classList.remove('open')
            overlay.classList.remove('show')
        })
    }

    /**
     * Adiciona um botão de logout na sidebar
     * Chamado automaticamente quando o usuário está autenticado
     */
    static addLogoutButton() {
        const sidebarFooter = document.querySelector('.sidebar-footer')
        if (!sidebarFooter) return

        // Verifica se já existe
        if (sidebarFooter.querySelector('.logout-btn')) return

        const logoutBtn = document.createElement('button')
        logoutBtn.className = 'sidebar-link logout-btn'
        logoutBtn.type = 'button'
        logoutBtn.innerHTML = `${Icons.logout()} Sair`

        logoutBtn.addEventListener('click', () => {
            $.ajax({
                url: '/api/auth/logout',
                type: 'POST',
                async: false,
                complete: () => {
                    globalThis.location.href = '/pages/Login.html'
                }
            })
        })

        sidebarFooter.appendChild(logoutBtn)
    }
}
