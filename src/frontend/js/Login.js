let imgSrcArray = ['../images/showing.png', '../images/hiding.png']
let currentImgIndex = 0

function trocar(elementId) {
    if (currentImgIndex === imgSrcArray.length)
        currentImgIndex = 0;
    document.getElementById(elementId).src = imgSrcArray[currentImgIndex];
    currentImgIndex++;
}

let passwordInput = document.getElementById('password-input');
let passwordToggle = document.querySelector('#password-img');

passwordToggle.addEventListener('click', function () {
    passwordInput.type = passwordInput.type === 'text' ? 'password' : 'text';
    trocar('password-img')
});

document.getElementById('login-btn').addEventListener("click", function () {
    let emailInput = document.getElementById('email-input').value
    let passwordInput = document.getElementById('password-input').value

    if (emailInput === '' || passwordInput === '')
        alert('Os campos Email e Senha devem ser preenchidos!');
    else
        document.form.submit();
});