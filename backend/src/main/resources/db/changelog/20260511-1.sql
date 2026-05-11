--liquibase formatted sql
--changeset github-copilot:20260511-1
--comment: migrate transaction.type from strings to numeric enum codes
UPDATE `transaction`
SET `type` = CASE
  WHEN `type` = 'debit' THEN 1
  WHEN `type` = 'credit' THEN 2
  ELSE NULL
END;

ALTER TABLE `transaction`
MODIFY COLUMN `type` INT NOT NULL;
