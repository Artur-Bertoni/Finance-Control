--liquibase formatted sql
--changeset artur:20260529-0
--comment: create user_feedback table for user feedback system

CREATE TABLE IF NOT EXISTS user_feedback (
    id         BIGINT AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    type       VARCHAR(20)  NOT NULL,
    message    TEXT         NOT NULL,
    nps_score  INT          NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
