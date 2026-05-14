--liquibase formatted sql
--changeset artur:20260514-0
--comment: deduplicate category_alias entries and add unique constraint

DELETE ca1
FROM `category_alias` ca1
INNER JOIN `category_alias` ca2
    ON ca1.category_id = ca2.category_id
    AND ca1.alias_name  = ca2.alias_name
    AND ca1.id > ca2.id;

ALTER TABLE `category_alias`
    ADD CONSTRAINT `uq_alias_category_name` UNIQUE (`category_id`, `alias_name`);
