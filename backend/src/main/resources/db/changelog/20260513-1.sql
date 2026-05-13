--liquibase formatted sql
--changeset artur:20260513-1
--comment: add language preference and admin flag to user table

ALTER TABLE `user`
    ADD COLUMN `language` VARCHAR(5)  NOT NULL DEFAULT 'pt',
    ADD COLUMN `admin`    BOOLEAN     NOT NULL DEFAULT FALSE;
