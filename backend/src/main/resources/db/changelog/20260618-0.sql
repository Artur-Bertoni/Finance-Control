--liquibase formatted sql
--changeset artur:20260618-0
--comment: Parcelamento automatico (parcelas filhas ligadas por grupo + flag de maturacao) e tipo de conta (cartao de credito)

ALTER TABLE account
    ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'CHECKING';

ALTER TABLE `transaction`
    ADD COLUMN installment_group_id BIGINT NULL,
    ADD COLUMN installment_index    INT NULL,
    ADD COLUMN applied              BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_transaction_installment_group ON `transaction` (installment_group_id);
CREATE INDEX idx_transaction_applied_date      ON `transaction` (applied, `date`);
