import { showToast } from '../utils/FrontendFunctions.js'
import { PasswordInput } from './components/PasswordInput.js'
import { ThemeManager } from './ThemeManager.js'

ThemeManager.initialize()

// Configurar toggle de visualização de senha
PasswordInput.setupToggle('password-input', 'password-img')

document.getElementById('register-btn').addEventListener('click', function () {
    globalThis.location.href = '/pages/User.html'
})

document.getElementById('login-btn').addEventListener('click', function () {
    const email    = document.getElementById('email-input').value
    const password = document.getElementById('password-input').value

    if (!email || !password) {
        showToast('Preencha e-mail e senha para continuar.', 'warning')
        return
    }

    $.ajax({
        url: '/api/auth/login',
        type: 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify({ email, password }),
        success: function () {
            globalThis.location.href = '/pages/HomePage.html'
        },
        error: function (xhr) {
            if (xhr.status === 401)
                showToast('E-mail ou senha incorretos.', 'error')
            else
                showToast('Erro ao fazer login. Tente novamente.', 'error')
        }
    })
})
