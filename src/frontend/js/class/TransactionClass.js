export class Transaction {
    constructor(id, account, category, transactionLocale, value, date, type, installmentsNumber) {
        this.id = id
        this.account = account
        this.category = category
        this.transactionLocale = transactionLocale
        this.value = value
        this.date = date
        this.type = type
        this.installmentsNumber = installmentsNumber
    }
}