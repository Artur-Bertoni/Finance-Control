--liquibase formatted sql
--changeset artur:20260821-1
--comment: configuracoes por usuario: habilitar/desabilitar funcionalidades do sistema (relatorios, orcamentos, metas, finny, importacao, instituicoes, locais e e-mails)

CREATE TABLE user_settings
(
    id                        BIGINT AUTO_INCREMENT,
    user_id                   BIGINT    NOT NULL,
    reports_enabled           BOOLEAN   NOT NULL DEFAULT TRUE,
    budgets_enabled           BOOLEAN   NOT NULL DEFAULT TRUE,
    goals_enabled             BOOLEAN   NOT NULL DEFAULT TRUE,
    finny_enabled             BOOLEAN   NOT NULL DEFAULT TRUE,
    statement_import_enabled  BOOLEAN   NOT NULL DEFAULT TRUE,
    institutions_enabled      BOOLEAN   NOT NULL DEFAULT TRUE,
    locales_enabled           BOOLEAN   NOT NULL DEFAULT TRUE,
    emails_enabled            BOOLEAN   NOT NULL DEFAULT TRUE,
    updated_at                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_settings_user UNIQUE (user_id)
);

INSERT INTO user_settings (user_id) SELECT id FROM user;
