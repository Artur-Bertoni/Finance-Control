--liquibase formatted sql
--changeset artur:20260515-3
ALTER TABLE account ADD COLUMN icon_key VARCHAR(100) NULL;
ALTER TABLE financial_institution ADD COLUMN icon_key VARCHAR(100) NULL;
ALTER TABLE transaction_locale ADD COLUMN icon_key VARCHAR(100) NULL;
