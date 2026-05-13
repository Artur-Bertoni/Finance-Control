--liquibase formatted sql
--changeset artur:20260513-3
--comment: widen category.name to 500 to accommodate long PDF descriptions

ALTER TABLE `category` MODIFY COLUMN `name` VARCHAR(500) NOT NULL;
