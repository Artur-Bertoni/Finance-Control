import {doRequest} from "../../utils/FrontendFunctions.js";
import {Category} from "./class/CategoryClass.js";

let data;
let categories = []

data = doRequest('http://localhost/finance-control/src/backend/resources/CategoryResource.php',
    {findAllByUser: true})

try {
    for (const element of data) {
        const category = Category.processCategory(element)
        categories.push(category)
    }
} catch(e) {
    console.log('No categories recovered from DB: ' + e)
}

let list = document.getElementById('categories-list')

for (const element of categories) {
    let button = document.createElement('button')
    button.classList.add('dashboard-item-btn')
    button.classList.add('colorful-button')
    button.type = "submit"
    button.name = "itemButton"
    button.value = element.id
    button.style.backgroundColor = '#4BAE50FF'

    let grid = document.createElement('div')
    grid.classList.add('dashboard-grid')

    let nameLabel = document.createElement('span')
    nameLabel.classList.add('grid-label')
    nameLabel.innerText = `Nome: ${element.name}`
    grid.appendChild(nameLabel)

    button.appendChild(grid)
    list.appendChild(button)
}