--liquibase formatted sql
--changeset artur:20260821-0
--comment: vinculos opcionais deixam de cascatear exclusao: excluir local desvincula as transacoes e excluir instituicao desvincula as contas

SET @fk = (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transaction' AND COLUMN_NAME = 'transaction_locale_id' AND REFERENCED_TABLE_NAME = 'transaction_locale' LIMIT 1);
SET @sql = IF(@fk IS NOT NULL, CONCAT('ALTER TABLE `transaction` DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `transaction`
    ADD CONSTRAINT fk_tx_locale FOREIGN KEY (transaction_locale_id) REFERENCES transaction_locale(id) ON DELETE SET NULL;

SET @fk = (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'account' AND COLUMN_NAME = 'financial_institution_id' AND REFERENCED_TABLE_NAME = 'financial_institution' LIMIT 1);
SET @sql = IF(@fk IS NOT NULL, CONCAT('ALTER TABLE `account` DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE account
    ADD CONSTRAINT fk_account_fi FOREIGN KEY (financial_institution_id) REFERENCES financial_institution(id) ON DELETE SET NULL;
