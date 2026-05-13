export class Transaction {
    constructor(id, account, category, transactionLocale, value, date, type, installmentsNumber, obs, transferPartnerId) {
        this.id = id
        this.account = account
        this.category = category
        this.transactionLocale = transactionLocale
        this.value = value
        this.date = date
        this.type = type
        this.installmentsNumber = installmentsNumber
        this.obs = obs
        this.transferPartnerId = transferPartnerId
    }

    static processTransaction(data) {
        return new Transaction(
            Number(data.id),
            data.account?.name ?? '',
            data.category?.name ?? '',
            data.transactionLocale?.name ?? null,
            Number(data.value),
            data.date,
            data.type,
            Number(data.installmentsNumber),
            data.obs ?? '',
            Number(data.transferPartnerId ?? 0)
        )
    }

    static formatLabel(tx) {
        const d = new Date(tx.date)
        const dateStr = `${d.getUTCDate().toString().padStart(2,'0')}/${(d.getUTCMonth()+1).toString().padStart(2,'0')}/${d.getUTCFullYear()}`
        return `${tx.category} – ${dateStr}`
    }
}
