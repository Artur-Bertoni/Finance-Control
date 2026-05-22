--liquibase formatted sql
--changeset artur:20260521-0
--comment: autenticação JWT - email_verified, oauth provider e tabela de verificação de email

ALTER TABLE `user`
    ADD COLUMN `email_verified`  BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN `provider`        VARCHAR(20)  NULL,
    ADD COLUMN `provider_id`     VARCHAR(255) NULL;

-- Usuários existentes: marcar como não verificados (pedirão verificação no próximo login)
-- Se quiser marcá-los como verificados automaticamente, mude FALSE para TRUE acima

CREATE TABLE `email_verification_token` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `token`      VARCHAR(64)  NOT NULL UNIQUE,
    `created_at` DATETIME     NOT NULL,
    `expires_at` DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_evt_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);
