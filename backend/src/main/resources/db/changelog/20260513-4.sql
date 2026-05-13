--liquibase formatted sql
--changeset artur:20260513-4
--comment: backfill internal_name with name for categories that predate the import feature

UPDATE `category` SET `internal_name` = `name` WHERE `internal_name` IS NULL;
