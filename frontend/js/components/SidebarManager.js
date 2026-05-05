/**
 * Gerenciador de Sidebar
 * Centraliza toda a lógica de navegação e interatividade da barra lateral
 * Substitui os scripts inline minificados
 */

export class SidebarManager {
    /**
     * Inicializa a sidebar com todos os comportamentos
     */
    static initialize() {
        SidebarManager.setupActiveLink()
        SidebarManager.setupToggleButton()
        SidebarManager.setupOverlayDismiss()
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
        logoutBtn.innerHTML = `
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd"/>
            </svg>
            Sair
        `

        logoutBtn.addEventListener('click', () => {
            $.ajax({
                url: '/api/auth/logout',
                type: 'POST',
                async: false,
                complete: () => {
                    window.location.href = '/pages/Login.html'
                }
            })
        })

        sidebarFooter.appendChild(logoutBtn)
    }
}
