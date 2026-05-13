--liquibase formatted sql
--changeset artur:20260513-0
--comment: add email notification preferences to user table

ALTER TABLE `user`
    ADD COLUMN `email_notification_enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN `email_notification_day`     TINYINT NOT NULL DEFAULT 5;
