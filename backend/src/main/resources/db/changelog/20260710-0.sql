--liquibase formatted sql
--changeset artur:20260710-0
--comment: reconciliacao de fatura (payment_transaction_id liga o pagamento ao lancamento importado do extrato) e limite de credito do cartao

ALTER TABLE credit_card_invoice_payment
    ADD COLUMN payment_transaction_id BIGINT NULL,
    ADD CONSTRAINT fk_ccip_payment_tx FOREIGN KEY (payment_transaction_id) REFERENCES `transaction`(id) ON DELETE SET NULL;

ALTER TABLE account
    ADD COLUMN credit_limit DOUBLE NULL;
