import {addDeleteIcon, doRequest, navigate} from "../utils/FrontendFunctions.js"
import {Category} from "./class/CategoryClass.js"

const urlParams = new URLSearchParams(window.location.search)
const categoryId = urlParams.get('id')

document.querySelector('form[name=form]').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'cancelButton') navigate('/pages/CategoryDashboard.html')
})

if (categoryId) {
    let response = doRequest(`/api/categories/${categoryId}`, 'GET')
    if (response?.id !== undefined) {
        let category = Category.processCategory(response)
        document.getElementById('name-input').value = category.name ?? ''
        document.getElementById('description-input').value = category.description ?? ''

        let deleteBtn = addDeleteIcon()
        deleteBtn.addEventListener('click', function () {
            $.ajax({
                url: `/api/categories/${categoryId}`,
                type: 'DELETE',
                async: false,
                success: function () { navigate('/pages/CategoryDashboard.html') },
                error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao excluir categoria.') }
            })
        })
    }
}

document.getElementById('save-btn').addEventListener('click', function () {
    let name = document.getElementById('name-input').value

    if (!name) {
        alert('O campo Nome deve ser preenchido!')
        return
    }

    let body = {
        name,
        description: document.getElementById('description-input').value || null
    }

    $.ajax({
        url: categoryId ? `/api/categories/${categoryId}` : '/api/categories',
        type: categoryId ? 'PUT' : 'POST',
        async: false,
        contentType: 'application/json',
        data: JSON.stringify(body),
        success: function () { navigate('/pages/CategoryDashboard.html') },
        error: function (xhr) { alert(xhr.responseJSON?.message ?? 'Erro ao salvar categoria.') }
    })
})
