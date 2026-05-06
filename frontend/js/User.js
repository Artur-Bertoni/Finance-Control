import { addDeleteIcon, navigate, showConfirm, showToast } from '../utils/FrontendFunctions.js'
import { PasswordInput } from './components/PasswordInput.js'
import { SidebarManager } from './components/SidebarManager.js'
import { Icons } from './icons/IconLibrary.js'

let currentUser = null

// Configurar toggle de visualização de senha
PasswordInput.setupToggle('password-input', 'password-img')
PasswordInput.setupToggle('password-confirm-input', 'password-confirm-img')

// Inicializar sidebar
SidebarManager.initialize()

loadUserData()

function loadUserData() {
    $.ajax({
        url:   '/api/auth/me',
        type:  'GET',
        async: false,
        success: function (user) {
            currentUser = user
            document.getElementById('username-input').value = user.username ?? ''
            document.getElementById('email-input').value    = user.email    ?? ''

            const logoutBtn = document.createElement('button')
            logoutBtn.className = 'btn btn-ghost btn-sm'
            logoutBtn.type = 'button'
            logoutBtn.innerHTML = `${Icons.logout()}<span style="margin-left:5px">Sair</span>`
            logoutBtn.addEventListener('click', () => {
                $.ajax({ url: '/api/auth/logout', type: 'POST', async: false, complete: () => navigate('/pages/Login.html') })
            })
            document.getElementById('header-actions').appendChild(logoutBtn)

            const deleteBtn = addDeleteIcon()
            deleteBtn.addEventListener('click', function () {
                showConfirm(
                    'Deseja excluir sua conta? Esta ação não pode ser desfeita.',
                    deleteUser,
                    'Excluir Conta'
                )
            })
        },
        error: function () {
            document.body.classList.add('user-guest')

            const main = document.querySelector('.page-content')
            const header = document.createElement('div')
            header.className = 'guest-form-header'
            header.innerHTML = '<img src="../images/logo.png" alt="Finance Control"><h1>Criar Conta</h1><p>Preencha os dados para acessar o Finance Control</p>'
            main.insertBefore(header, main.firstChild)

            document.getElementById('page-title-text').textContent = 'Criar Conta'
            document.getElementById('save-btn').textContent        = 'Criar Conta'
            document.getElementById('cancel-btn').textContent      = 'Voltar ao Login'
        }
    })
}

document.getElementById('cancel-btn').addEventListener('click', () =>
    navigate(currentUser ? '/pages/HomePage.html' : '/pages/Login.html')
)

document.getElementById('save-btn').addEventListener('click', function () {
    const username             = document.getElementById('username-input').value
    const email                = document.getElementById('email-input').value
    const password             = document.getElementById('password-input').value
    const passwordConfirmation = document.getElementById('password-confirm-input').value

    if (!username || !email || !password || !passwordConfirmation) {
        showToast('Os campos Nome, Email, Senha e Confirmar Senha são obrigatórios.', 'warning')
        return
    }

    const isNewUser = !currentUser
    
    $.ajax({
        url:         isNewUser ? '/api/users' : `/api/users/${currentUser.id}`,
        type:        isNewUser ? 'POST' : 'PUT',
        async:       false,
        contentType: 'application/json',
        data:        JSON.stringify({ username, email, password, passwordConfirmation }),
        success:     function () {
            if (isNewUser) {
                // Fazer login automático após cadastro bem-sucedido
                $.ajax({
                    url: '/api/auth/login',
                    type: 'POST',
                    async: false,
                    contentType: 'application/json',
                    data: JSON.stringify({ email, password }),
                    success: function () {
                        showToast('Conta criada e login realizado com sucesso!', 'success')
                        navigate('/pages/HomePage.html')
                    },
                    error: function (xhr) {
                        showToast('Conta criada, mas falha ao fazer login automático. Redirecionando para login.', 'warning')
                        navigate('/pages/Login.html')
                    }
                })
            } else {
                navigate('/pages/HomePage.html')
            }
        },
        error:       function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao salvar usuário.', 'error') }
    })
})

function deleteUser() {
    $.ajax({
        url:   `/api/users/${currentUser.id}`,
        type:  'DELETE',
        async: false,
        success: function () { navigate('/pages/Login.html') },
        error:   function (xhr) { showToast(xhr.responseJSON?.message ?? 'Erro ao excluir usuário.', 'error') }
    })
}
