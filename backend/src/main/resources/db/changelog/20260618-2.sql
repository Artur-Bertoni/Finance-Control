--liquibase formatted sql
--changeset artur:20260618-2
--comment: Flag 'seeded' em category e account para distinguir dados semeados no cadastro (nao contam para conquistas)

ALTER TABLE category
    ADD COLUMN seeded BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE account
    ADD COLUMN seeded BOOLEAN NOT NULL DEFAULT FALSE;
