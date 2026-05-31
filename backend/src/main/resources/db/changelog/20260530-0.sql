--liquibase formatted sql

--changeset financecontrol:30
-- Índice composto para as consultas mais frequentes (dashboard e listagem de
-- transações), que filtram por usuário e faixa de datas ao mesmo tempo.
-- A FK user_id já tem índice de coluna única; este cobre melhor o predicado
-- WHERE user_id = ? AND date BETWEEN ? AND ?.
CREATE INDEX idx_transaction_user_date ON transaction(user_id, date);
