--liquibase formatted sql
--changeset artur:20260618-3
--comment: Orcamento mensal por categoria (planejado vs gasto no mes)

CREATE TABLE budget
(
    id            BIGINT AUTO_INCREMENT,
    user_id       BIGINT    NOT NULL,
    category_id   BIGINT    NOT NULL,
    monthly_limit DOUBLE    NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_budget_user_category UNIQUE (user_id, category_id)
);

CREATE INDEX idx_budget_user ON budget (user_id);
