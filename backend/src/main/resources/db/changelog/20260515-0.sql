--liquibase formatted sql
--changeset artur:20260515-0
--comment: add goal_email_notification_enabled flag to user

ALTER TABLE user ADD COLUMN goal_email_notification_enabled BOOLEAN NOT NULL DEFAULT TRUE;
