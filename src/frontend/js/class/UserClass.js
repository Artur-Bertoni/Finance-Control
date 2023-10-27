export class User {
    constructor(id, username, email, password) {
        this.id = id
        this.username = username
        this.email = email
        this.password = password
    }

    static processUser(data) {
        return new User(
            Number(data.id),
            data.username,
            data.email,
            data.password
        );
    }
}