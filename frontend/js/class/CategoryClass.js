import { populateSelect } from '../../utils/FrontendFunctions.js'

export class Category {
    constructor(id, name, description, iconKey, aliases) {
        this.id = id
        this.name = name
        this.description = description
        this.iconKey = iconKey ?? null
        this.aliases = aliases ?? []
    }

    static addCategories(elementId) {
        populateSelect(elementId, '/api/categories', 'iconKey')
    }

    static processCategory(data) {
        return new Category(
            Number(data.id),
            data.name,
            data.description ?? '',
            data.iconKey ?? null,
            data.aliases ?? []
        )
    }
}
