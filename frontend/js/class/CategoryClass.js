import { populateSelect } from '../../utils/FrontendFunctions.js'

export class Category {
    constructor(id, name, description, internalName) {
        this.id = id
        this.name = name
        this.description = description
        this.internalName = internalName ?? null
    }

    static addCategories(elementId) {
        populateSelect(elementId, '/api/categories')
    }

    static processCategory(data) {
        return new Category(
            Number(data.id),
            data.name,
            data.description ?? '',
            data.internalName ?? null
        )
    }
}
