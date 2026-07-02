--liquibase formatted sql
--changeset artur:20260702-2
--comment: flag onboarding_completed no usuario para o tour guiado ser persistido por conta (nao repetir em navegadores/dispositivos diferentes)

ALTER TABLE `user`
    ADD COLUMN `onboarding_completed` BOOLEAN NOT NULL DEFAULT FALSE;

-- usuarios ja existentes nao devem ver o tour novamente
UPDATE `user` SET `onboarding_completed` = TRUE;
