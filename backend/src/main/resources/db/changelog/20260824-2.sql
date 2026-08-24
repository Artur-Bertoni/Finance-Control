--liquibase formatted sql
--changeset artur:20260824-2
--comment: categorias que o usuario escolheu nao exibir nos graficos de pizza do dashboard, por grafico

ALTER TABLE user_settings ADD COLUMN chart_expense_hidden_categories VARCHAR(2000) NULL;
ALTER TABLE user_settings ADD COLUMN chart_income_hidden_categories VARCHAR(2000) NULL;
