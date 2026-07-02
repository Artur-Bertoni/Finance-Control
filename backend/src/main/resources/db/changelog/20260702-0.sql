--liquibase formatted sql
--changeset artur:20260702-0
--comment: instituicao financeira opcional em account (carteira/dinheiro nao pertence a um banco)

ALTER TABLE account
    MODIFY COLUMN financial_institution_id BIGINT NULL;
