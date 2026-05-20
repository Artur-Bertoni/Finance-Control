--liquibase formatted sql
--changeset artur:20260520-2
--comment: make end_date nullable on financial_goal

ALTER TABLE `financial_goal`
    MODIFY COLUMN `end_date` DATE NULL;
