--liquibase formatted sql
--changeset artur:20260513-2
--comment: add internal_name to category for statement import matching

ALTER TABLE `category`
    ADD COLUMN `internal_name` VARCHAR(500) NULL;
