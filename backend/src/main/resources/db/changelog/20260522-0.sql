--liquibase formatted sql
--changeset artur:20260522-0
--comment: allow null password for OAuth2-only users

ALTER TABLE `user` MODIFY `password` VARCHAR(75) NULL;
