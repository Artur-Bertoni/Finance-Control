--liquibase formatted sql
--changeset artur:20260710-1
--comment: referencia explicita de fatura no lancamento (yyyy-MM) para importacao de fatura respeitar o agrupamento do banco em vez de re-derivar pela data

ALTER TABLE `transaction`
    ADD COLUMN invoice_reference VARCHAR(7) NULL;
