import {addDeleteIcon, doRequest} from "../../utils/FrontendFunctions.js";
import {Category} from "./class/CategoryClass.js";

tryToPopulateWithData();

function tryToPopulateWithData() {
    let response = doRequest(
        'http://localhost/finance-control/src/backend/resources/CategoryResource.php',
        {findById: true})

    let category = Category.processCategory(response)
    let nameInput = document.getElementById('name-input')
    let descriptionInput = document.getElementById('description-input')

    if (category.name !== undefined)
        nameInput.value = category.name
    if (category.description !== undefined)
        descriptionInput.value = category.description

    addDeleteIcon()
}