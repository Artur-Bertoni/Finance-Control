import {doRequest, navigate} from "../utils/FrontendFunctions.js"
import {Category} from "./class/CategoryClass.js"

let categories = []

try {
    let data = doRequest('/api/categories', 'GET')
    for (const element of (data ?? [])) {
        categories.push(Category.processCategory(element))
    }
} catch (e) {
    console.log('No categories recovered from DB: ' + e)
}

document.getElementById('dashboard-form').addEventListener('submit', function (e) {
    e.preventDefault()
    const name = e.submitter?.name
    if (name === 'profileButton') navigate('/pages/User.html')
    else if (name === 'homeButton') navigate('/pages/HomePage.html')
    else if (name === 'postCategory') navigate('/pages/Category.html')
})

let list = document.getElementById('categories-list')

for (const element of categories) {
    let button = document.createElement('button')
    button.classList.add('dashboard-item-btn')
    button.classList.add('colorful-button')
    button.type = 'button'
    button.style.backgroundColor = '#4BAE50FF'
    button.addEventListener('click', () => navigate(`/pages/Category.html?id=${element.id}`))

    let grid = document.createElement('div')
    grid.classList.add('dashboard-grid')

    let nameLabel = document.createElement('span')
    nameLabel.classList.add('grid-label')
    nameLabel.innerText = `Nome: ${element.name}`
    grid.appendChild(nameLabel)

    button.appendChild(grid)
    list.appendChild(button)
}
