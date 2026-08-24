--liquibase formatted sql
--changeset artur:20260824-1
--comment: separa a configuracao da legenda dos graficos de pizza: despesas e receitas passam a ter listas independentes

ALTER TABLE user_settings RENAME COLUMN chart_pinned_categories TO chart_expense_pinned_categories;
ALTER TABLE user_settings RENAME COLUMN chart_grouped_categories TO chart_expense_grouped_categories;
ALTER TABLE user_settings ADD COLUMN chart_income_pinned_categories VARCHAR(2000) NULL;
ALTER TABLE user_settings ADD COLUMN chart_income_grouped_categories VARCHAR(2000) NULL;
