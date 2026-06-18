--liquibase formatted sql
--changeset artur:20260618-1
--comment: Fase B do cartao de credito: dias de fechamento/vencimento na conta e tabela de pagamento de fatura

ALTER TABLE account
    ADD COLUMN closing_day INT NULL,
    ADD COLUMN due_day     INT NULL;

CREATE TABLE credit_card_invoice_payment
(
    id                BIGINT AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    account_id        BIGINT       NOT NULL,
    reference_month   VARCHAR(7)   NOT NULL,
    value             DOUBLE       NOT NULL,
    source_account_id BIGINT,
    paid_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_invoice_payment_account_ref UNIQUE (account_id, reference_month)
);

CREATE INDEX idx_invoice_payment_account ON credit_card_invoice_payment (account_id);
