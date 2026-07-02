--liquibase formatted sql
--changeset artur:20260702-1
--comment: flag notified em user_achievement para toast de conquista disparar uma unica vez por usuario (independente de dispositivo/navegador)

ALTER TABLE user_achievement
    ADD COLUMN `notified` BOOLEAN NOT NULL DEFAULT FALSE;

-- conquistas ja existentes sao consideradas ja notificadas para nao inundar de toasts no primeiro login apos o deploy
UPDATE user_achievement SET `notified` = TRUE;
