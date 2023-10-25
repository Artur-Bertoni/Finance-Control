import {doRequest} from "../../../utils/functions.js";

export class Category {
    constructor(id, name, description) {
        this.id = id
        this.name = name
        this.description = description
    }

    static addCategories() {
        let categories = this.processCategories(doRequest(
            'http://localhost/finance-control/src/backend/resources/CategoryResource.php',
            {findAllByUser: true}));

        let categoryList = document.getElementById('category-input')

        for (const element of categories) {
            let option = document.createElement('option')
            option.value = element.id
            option.innerText = element.name

            categoryList.appendChild(option)
        }
    }

    static processCategories(data) {
        let array = [];
        for (const element of data) {
            const categoryData = element;
            const category = new Category(
                Number(categoryData.id),
                categoryData.name,
                categoryData.description
            );

            array.push(category);
        }

        return array;
    }
}