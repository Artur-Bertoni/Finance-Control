import {doRequest} from "../../../utils/FrontendFunctions.js";

export class Category {
    constructor(id, name, description) {
        this.id = id
        this.name = name
        this.description = description
    }

    static addCategories() {
        let categories = doRequest(
            'http://localhost/finance-control/src/backend/resources/CategoryResource.php',
            {findAllByUser: true}
        );

        let categoryList = document.getElementById('category-input')

        for (const element of categories) {
            let category = this.processCategory(element)
            let option = document.createElement('option')
            option.value = category.id
            option.innerText = category.name

            categoryList.appendChild(option)
        }
    }

    static processCategory(data) {
        return new Category(
            Number(data.id),
            data.name,
            data.description
        );
    }
}