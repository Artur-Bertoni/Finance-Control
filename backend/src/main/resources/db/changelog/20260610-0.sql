--liquibase formatted sql
--changeset artur:20260610-0
--comment: Finny AI agent - tabelas de histórico de dicas (finny_tip) e preferências aprendidas (finny_tip_preference)

CREATE TABLE finny_tip
(
    id          BIGINT AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    rule_key    VARCHAR(60)  NOT NULL,
    category    VARCHAR(30)  NOT NULL,
    params_json TEXT,
    severity    VARCHAR(20)  NOT NULL,
    score       DOUBLE       NOT NULL DEFAULT 0,
    status      VARCHAR(20)  NOT NULL,
    lang        VARCHAR(10),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shown_at    TIMESTAMP    NULL,
    feedback_at TIMESTAMP    NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE INDEX idx_finny_tip_user_created ON finny_tip (user_id, created_at);
CREATE INDEX idx_finny_tip_user_rule    ON finny_tip (user_id, rule_key);
CREATE INDEX idx_finny_tip_user_status  ON finny_tip (user_id, status);

CREATE TABLE finny_tip_preference
(
    id                BIGINT AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    category          VARCHAR(30)  NOT NULL,
    weight            DOUBLE       NOT NULL DEFAULT 1.0,
    helpful_count     INT          NOT NULL DEFAULT 0,
    not_helpful_count INT          NOT NULL DEFAULT 0,
    dismissed_count   INT          NOT NULL DEFAULT 0,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT uq_finny_pref_user_category UNIQUE (user_id, category)
);

CREATE INDEX idx_finny_pref_user ON finny_tip_preference (user_id);
