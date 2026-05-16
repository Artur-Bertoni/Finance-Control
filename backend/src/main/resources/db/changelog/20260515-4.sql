--liquibase formatted sql
--changeset artur:20260515-4
--comment: in-app notifications — app_notification table

CREATE TABLE app_notification
(
    id             INT AUTO_INCREMENT,
    user_id        INT          NOT NULL,
    type           VARCHAR(50)  NOT NULL,
    goal_id        INT,
    goal_name      VARCHAR(100),
    transaction_id INT,
    link           VARCHAR(500),
    is_read        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE INDEX idx_app_notification_user_id ON app_notification (user_id);
CREATE INDEX idx_app_notification_unread  ON app_notification (user_id, is_read);
