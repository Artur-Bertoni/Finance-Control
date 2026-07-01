--liquibase formatted sql
--changeset artur:20260629-0
--comment: exclusao logica de usuario (coluna active) e FKs faltantes em budget, credit_card_invoice_payment e user_achievement

-- 1) Exclusao logica: flag de usuario ativo/inativo
ALTER TABLE `user`
    ADD COLUMN `active` BOOLEAN NOT NULL DEFAULT TRUE;

-- 2) Limpeza de orfaos deixados por exclusoes fisicas anteriores (estas tabelas nao tinham FK)
DELETE FROM budget WHERE user_id NOT IN (SELECT id FROM `user`);
DELETE FROM budget WHERE category_id NOT IN (SELECT id FROM category);
DELETE FROM credit_card_invoice_payment WHERE user_id NOT IN (SELECT id FROM `user`);
DELETE FROM credit_card_invoice_payment WHERE account_id NOT IN (SELECT id FROM account);
UPDATE credit_card_invoice_payment
    SET source_account_id = NULL
    WHERE source_account_id IS NOT NULL
      AND source_account_id NOT IN (SELECT id FROM account);
DELETE FROM user_achievement WHERE user_id NOT IN (SELECT id FROM `user`);

-- 3) FKs faltantes
ALTER TABLE budget
    ADD CONSTRAINT fk_budget_user     FOREIGN KEY (user_id)     REFERENCES `user`(id)  ON DELETE CASCADE,
    ADD CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE;

ALTER TABLE credit_card_invoice_payment
    ADD CONSTRAINT fk_ccip_user           FOREIGN KEY (user_id)           REFERENCES `user`(id)  ON DELETE CASCADE,
    ADD CONSTRAINT fk_ccip_account        FOREIGN KEY (account_id)        REFERENCES account(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_ccip_source_account FOREIGN KEY (source_account_id) REFERENCES account(id) ON DELETE SET NULL;

ALTER TABLE user_achievement
    ADD CONSTRAINT fk_user_achievement_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE;
