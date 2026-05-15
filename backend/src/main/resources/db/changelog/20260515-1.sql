--liquibase formatted sql
--changeset artur:20260515-1
--comment: add icon_key column to category table

ALTER TABLE category ADD COLUMN icon_key VARCHAR(100) NULL;
