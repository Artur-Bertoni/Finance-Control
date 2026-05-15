--liquibase formatted sql
--changeset artur:20260515-2
--comment: create user_achievement table for rewards system

CREATE TABLE user_achievement (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    achievement_type VARCHAR(60)  NOT NULL,
    earned_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_achievement (user_id, achievement_type)
);
