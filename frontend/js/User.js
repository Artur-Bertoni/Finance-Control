import {addDeleteIcon, addHomePageIcon, navigate} from "../utils/FrontendFunctions.js"

addPasswordVisualization()

let currentUser = null

loadUserData()

function loadUserData() {
    $.ajax({
        url: '/api/auth/me',
        type: 'GET',
        async: false,
        success: function (user) {
            currentUser = user
            document.getElementById('username-input').value = user.username ?? ''
            document.getElementById('email-input').value = user.email ?? ''

            let homeBtn = addHomePageIcon()
            homeBtn.addEventListener('click', () => navigate('/pages/HomePage.html'))

            let deleteBtn = addDeleteIcon()
            deleteBtn.addEventListener('click', deleteUser)
        },
        error: function () {}
    })
}

document.querySelector('form[name=form]').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'cancelButton') navigate(currentUser ? '/pages/HomePage.html' : '/pages/Login.html')
})

document.getElementById('save-btn').addEventListener('click', function () {
    let username = document.getElementById('username-input').value
    let email = document.getElementById('email-input').value
    let password = document.getElementById('password-input').value
    let passwordConfirmation = document.getElementById('password-confirm-input').value

    if (!username || !email || !password || !passwordConfirmation) {
        alert('Os campos Nome de Usuário, Endereço de Email, Senha e Confirmar Senha devem ser preenchidos!')
        return
    }

    $.ajax({
        url: currentUser ? `/api/users/${currentUser.id}` : '/api/users',
        type: currentUser ? 'PUT' : 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify({ username, email, password, passwordConfirmation }),
        success: function () {
            navigate(currentUser ? '/pages/HomePage.html' : '/pages/Login.html')
        },
        error: function (xhr) {
            alert(xhr.responseJSON?.message ?? 'Erro ao salvar usuário.')
        }
    })
})

function deleteUser() {
    if (!confirm('Deseja excluir sua conta? Esta ação não pode ser desfeita.')) return
    $.ajax({
        url: `/api/users/${currentUser.id}`,
        type: 'DELETE',
        async: false,
        success: function () { navigate('/pages/Login.html') },
        error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao excluir usuário.') }
    })
}

function addPasswordVisualization() {
    let imgSrcArray = ['../images/showing.png', '../images/hiding.png']
    let currentImgIndex = 0

    function trocar(elementId) {
        if (currentImgIndex === imgSrcArray.length)
            currentImgIndex = 0
        document.getElementById(elementId).src = imgSrcArray[currentImgIndex]
        currentImgIndex++
    }

    let passwordInput = document.getElementById('password-input')
    let passwordToggle = document.querySelector('#password-img')
    passwordToggle.addEventListener('click', function () {
        passwordInput.type = passwordInput.type === 'text' ? 'password' : 'text'
        trocar('password-img')
    })

    let passwordConfirmInput = document.getElementById('password-confirm-input')
    let passwordConfirmToggle = document.querySelector('#password-confirm-img')
    passwordConfirmToggle.addEventListener('click', function () {
        passwordConfirmInput.type = passwordConfirmInput.type === 'text' ? 'password' : 'text'
        trocar('password-confirm-img')
    })
}
