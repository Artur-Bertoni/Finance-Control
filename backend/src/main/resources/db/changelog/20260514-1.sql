--liquibase formatted sql
--changeset artur:20260514-1
--comment: financial goals feature — goals, category/locale filters, notification log

CREATE TABLE financial_goal
(
    id                 INT AUTO_INCREMENT,
    user_id            INT          NOT NULL,
    name               VARCHAR(100) NOT NULL,
    description        TINYTEXT,
    type               VARCHAR(20)  NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    target_amount      DOUBLE       NOT NULL,
    start_date         DATE         NOT NULL,
    end_date           DATE         NOT NULL,
    notify_at_50       BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_at_75       BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_at_90       BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_on_complete BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_on_deadline BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_on_exceed   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE TABLE goal_category
(
    goal_id     INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (goal_id, category_id),
    FOREIGN KEY (goal_id)     REFERENCES financial_goal (id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category (id)       ON DELETE CASCADE
);

CREATE TABLE goal_locale
(
    goal_id   INT NOT NULL,
    locale_id INT NOT NULL,
    PRIMARY KEY (goal_id, locale_id),
    FOREIGN KEY (goal_id)   REFERENCES financial_goal (id)    ON DELETE CASCADE,
    FOREIGN KEY (locale_id) REFERENCES transaction_locale (id) ON DELETE CASCADE
);

CREATE TABLE goal_notification_log
(
    id                INT AUTO_INCREMENT,
    goal_id           INT         NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    sent_at           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_goal_notification (goal_id, notification_type),
    FOREIGN KEY (goal_id) REFERENCES financial_goal (id) ON DELETE CASCADE
);
