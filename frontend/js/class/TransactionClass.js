export class Transaction {
    constructor({ id, account, category, categoryIconKey, transactionLocale, value, date, type, installmentsNumber, obs, transferPartnerId }) {
        this.id = id
        this.account = account
        this.category = category
        this.categoryIconKey = categoryIconKey
        this.transactionLocale = transactionLocale
        this.value = value
        this.date = date
        this.type = type
        this.installmentsNumber = installmentsNumber
        this.obs = obs
        this.transferPartnerId = transferPartnerId
    }

    static processTransaction(data) {
        return new Transaction({
            id:                Number(data.id),
            account:           data.account?.name ?? '',
            category:          data.category?.name ?? '',
            categoryIconKey:   data.category?.iconKey ?? null,
            transactionLocale: data.transactionLocale?.name ?? null,
            value:             Number(data.value),
            date:              data.date,
            type:              data.type,
            installmentsNumber: Number(data.installmentsNumber),
            obs:               data.obs ?? '',
            transferPartnerId: Number(data.transferPartnerId ?? 0),
        })
    }

    static formatLabel(tx) {
        const d = new Date(tx.date)
        const dateStr = `${d.getUTCDate().toString().padStart(2,'0')}/${(d.getUTCMonth()+1).toString().padStart(2,'0')}/${d.getUTCFullYear()}`
        return `${tx.category} – ${dateStr}`
    }
}
