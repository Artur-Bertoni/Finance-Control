--liquibase formatted sql
--changeset artur:20260518-0
--comment: add message and severity columns to app_notification for user action history

ALTER TABLE app_notification
    ADD COLUMN message  VARCHAR(512) NULL,
    ADD COLUMN severity VARCHAR(20)  NULL;
