--liquibase formatted sql
--changeset artur:20260824-0
--comment: preferencia por usuario de quais categorias aparecem separadas e quais ficam agrupadas em "Outros" nos graficos de pizza do dashboard

ALTER TABLE user_settings ADD COLUMN chart_pinned_categories VARCHAR(2000) NULL;
ALTER TABLE user_settings ADD COLUMN chart_grouped_categories VARCHAR(2000) NULL;
