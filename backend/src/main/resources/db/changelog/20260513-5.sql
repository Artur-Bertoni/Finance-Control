--liquibase formatted sql
--changeset artur:20260513-5
--comment: create category_alias table for multiple internal name mappings per category

CREATE TABLE `category_alias` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `category_id` INT          NOT NULL,
    `alias_name`  VARCHAR(500) NOT NULL,
    CONSTRAINT `fk_alias_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE CASCADE
);

INSERT INTO `category_alias` (`category_id`, `alias_name`)
SELECT `id`, `internal_name`
FROM `category`
WHERE `internal_name` IS NOT NULL AND `internal_name` != '';
