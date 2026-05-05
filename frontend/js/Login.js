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

document.getElementById('register-btn').addEventListener('click', function (e) {
    e.preventDefault()
    window.location.href = '/pages/User.html'
})

document.getElementById('login-btn').addEventListener('click', function () {
    let email = document.getElementById('email-input').value
    let password = document.getElementById('password-input').value

    if (!email || !password) {
        alert('Os campos Email e Senha devem ser preenchidos!')
        return
    }

    $.ajax({
        url: '/api/auth/login',
        type: 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify({ email, password }),
        success: function () {
            window.location.href = '/pages/HomePage.html'
        },
        error: function (xhr) {
            if (xhr.status === 401)
                alert('Email ou senha incorretos!')
            else
                alert('Erro ao fazer login.')
        }
    })
})
