import {addDeleteIcon, addHomePageIcon, doRequest} from "../../utils/FrontendFunctions.js";
import {User} from "./class/UserClass.js";

addPasswordVisualization();
tryToPopulateWithData();

document.getElementById('save-btn').addEventListener("click", function () {
    let usernameInput = document.getElementById('username-input').value
    let emailInput = document.getElementById('email-input').value
    let passwordInput = document.getElementById('password-input').value
    let passwordConfirmInput = document.getElementById('password-confirm-input').value

    if (usernameInput === '' || emailInput === '' || passwordInput === '' || passwordConfirmInput === '')
        alert('Os campos Nome de Usuário, Endereço de Email, Senha e Confirmar Senha devem ser preenchidos!');
    else
        document.form.submit();
});

function tryToPopulateWithData() {
    let response = doRequest(
        'http://localhost/finance-control/src/backend/resources/UserResource.php',
        {findById: true})

    if (response) {
        let user = User.processUser(response)
        let usernameInput = document.getElementById('username-input')
        let emailInput = document.getElementById('email-input')
        let passwordInput = document.getElementById('password-input')
        let passwordConfirmInput = document.getElementById('password-confirm-input')
        if (user.username !== undefined)
            usernameInput.value = user.username
        if (user.email !== undefined)
            emailInput.value = user.email
        if (user.password !== undefined) {
            passwordInput.value = user.password
            passwordConfirmInput.value = user.password
        }

        addHomePageIcon()
        addDeleteIcon()
    }
}

function addPasswordVisualization() {
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

    let passwordConfirmInput = document.getElementById('password-confirm-input');
    let passwordConfirmToggle = document.querySelector('#password-confirm-img');

    passwordConfirmToggle.addEventListener('click', function () {
        passwordConfirmInput.type = passwordConfirmInput.type === 'text' ? 'password' : 'text';
        trocar('password-confirm-img')
    });
}