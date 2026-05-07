-- ============================================================
--  TEST DATA  –  user_id = 4
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `transaction`;
TRUNCATE TABLE `account`;
TRUNCATE TABLE `category`;
TRUNCATE TABLE `transaction_locale`;
TRUNCATE TABLE `financial_institution`;
SET FOREIGN_KEY_CHECKS = 1;

-- ── FINANCIAL INSTITUTIONS ────────────────────────────────
INSERT INTO financial_institution (user_id, name, address, contact) VALUES
(4, 'Nubank',           'Rua Capote Valente, 39 – São Paulo/SP',      '0800 123 4000'),
(4, 'Itaú Unibanco',    'Praça Alfredo Egydio de Souza Aranha – SP',  '4004-4828'),
(4, 'Bradesco',         'Cidade de Deus – Osasco/SP',                 '0800 722 4848'),
(4, 'Banco do Brasil',  'SBS Quadra 01 – Brasília/DF',                '4004-0001'),
(4, 'XP Investimentos', 'Av. Chedid Jafet, 75 – São Paulo/SP',        '(11) 4935-2000');

-- ── ACCOUNTS ─────────────────────────────────────────────
INSERT INTO account (user_id, financial_institution_id, name, contact, description, balance) VALUES
(4, 1, 'Conta Nubank',   'app@nubank.com',   'Conta corrente digital',   2450.75),
(4, 2, 'Conta Itaú',     'app@itau.com',     'Conta corrente principal', 8120.50),
(4, 3, 'Conta Bradesco', 'app@bradesco.com', 'Conta poupança',           3200.00),
(4, 4, 'Conta BB',       'app@bb.com',       'Conta salário',            5890.30),
(4, 5, 'Carteira XP',    'app@xpi.com',      'Conta investimentos',      15000.00);

-- ── CATEGORIES ───────────────────────────────────────────
INSERT INTO category (user_id, name, description) VALUES
(4, 'Alimentação', 'Mercado, restaurantes, delivery'),
(4, 'Transporte',  'Combustível, Uber, transporte público'),
(4, 'Saúde',       'Farmácia, plano de saúde, consultas'),
(4, 'Lazer',       'Cinema, jogos, viagens, entretenimento'),
(4, 'Moradia',     'Aluguel, condomínio, contas de casa');

-- ── TRANSACTION LOCALES ──────────────────────────────────
INSERT INTO transaction_locale (user_id, name, address) VALUES
(4, 'Supermercado Extra',  'Av. Paulista, 1000 – São Paulo/SP'),
(4, 'Posto Shell',         'Rua das Flores, 250 – São Paulo/SP'),
(4, 'Shopping Ibirapuera', 'Av. Ibirapuera, 3103 – São Paulo/SP'),
(4, 'Drogasil',            'Rua Augusta, 500 – São Paulo/SP'),
(4, 'Outback Steakhouse',  'Shopping Paulista, Piso L2 – SP');

-- ── TRANSACTIONS (70 rows) ───────────────────────────────
-- Columns: user_id, account_id, category_id, transaction_locale_id,
--          value, date, type, installments_number, obs, transfer_partner_id
INSERT INTO `transaction`
    (user_id, account_id, category_id, transaction_locale_id, value, date, type, installments_number, obs, transfer_partner_id)
VALUES
-- ▸ MARÇO
(4, 4, 1, 1,    245.80, '2026-03-01', 'debit',  0, 'Compras do mês',              NULL),
(4, 2, 5, NULL, 4500.00,'2026-03-05', 'credit', 0, 'Salário março',               NULL),
(4, 2, 5, NULL, 1800.00,'2026-03-05', 'debit',  0, 'Aluguel março',               NULL),
(4, 1, 2, 2,    120.00, '2026-03-07', 'debit',  0, 'Combustível',                 NULL),
(4, 1, 1, 5,     85.50, '2026-03-08', 'debit',  0, 'Jantar com amigos',           NULL),
(4, 3, 4, 3,    320.00, '2026-03-10', 'debit',  3, 'Eletrônico parcelado',        NULL),
(4, 2, 3, 4,     65.90, '2026-03-12', 'debit',  0, 'Remédios',                    NULL),
(4, 1, 2, NULL,  35.00, '2026-03-14', 'debit',  0, 'Uber',                        NULL),
(4, 4, 5, NULL, 5200.00,'2026-03-15', 'credit', 0, 'Salário março BB',            NULL),
(4, 4, 5, NULL, 2200.00,'2026-03-15', 'debit',  0, 'Aluguel BB',                  NULL),
(4, 1, 1, 1,    189.30, '2026-03-17', 'debit',  0, 'Mercado',                     NULL),
(4, 2, 4, 3,    450.00, '2026-03-18', 'debit',  6, 'Notebook parcelado',          NULL),
(4, 1, 3, 4,     42.00, '2026-03-20', 'debit',  0, 'Vitaminas',                   NULL),
(4, 4, 2, 2,    180.00, '2026-03-21', 'debit',  0, 'Gasolina',                    NULL),
(4, 1, 1, 5,     62.40, '2026-03-22', 'debit',  0, 'Almoço',                      NULL),
(4, 1, 1, NULL, 350.00, '2026-03-25', 'credit', 0, 'Reembolso empresa',           NULL),
(4, 3, 5, NULL, 800.00, '2026-03-25', 'debit',  0, 'Condomínio',                  NULL),
(4, 1, 2, NULL,  25.00, '2026-03-26', 'debit',  0, 'Ônibus mensal',               NULL),
(4, 2, 1, 1,    310.50, '2026-03-27', 'debit',  0, 'Compras semana',              NULL),
(4, 4, 3, NULL, 150.00, '2026-03-28', 'debit',  0, 'Plano de saúde',              NULL),
(4, 1, 4, NULL,  29.90, '2026-03-28', 'debit',  0, 'Streaming',                   NULL),
(4, 3, 4, NULL, 200.00, '2026-03-30', 'credit', 0, 'Cashback',                    NULL),
(4, 2, 1, 5,     78.90, '2026-03-30', 'debit',  0, 'Lanche',                      NULL),
(4, 4, 5, NULL, 350.00, '2026-03-31', 'debit',  0, 'IPTU parcela',                NULL),
(4, 1, 2, NULL, 140.00, '2026-03-31', 'debit',  0, 'Combustível extra',           NULL),
-- ▸ ABRIL
(4, 4, 5, NULL, 5200.00,'2026-04-01', 'credit', 0, 'Salário abril BB',            NULL),
(4, 4, 5, NULL, 2200.00,'2026-04-01', 'debit',  0, 'Aluguel abril',               NULL),
(4, 1, 1, 1,    215.40, '2026-04-02', 'debit',  0, 'Mercado',                     NULL),
(4, 2, 2, 2,     95.00, '2026-04-03', 'debit',  0, 'Combustível',                 NULL),
(4, 1, 4, NULL,  45.00, '2026-04-04', 'debit',  0, 'Netflix + Spotify',           NULL),
(4, 2, 5, NULL, 4500.00,'2026-04-05', 'credit', 0, 'Salário abril Itaú',          NULL),
(4, 2, 5, NULL, 1800.00,'2026-04-05', 'debit',  0, 'Aluguel Itaú',                NULL),
(4, 3, 3, 4,     95.00, '2026-04-06', 'debit',  0, 'Farmácia',                    NULL),
(4, 1, 1, 5,     72.50, '2026-04-07', 'debit',  0, 'Jantar',                      NULL),
(4, 4, 2, NULL,  30.00, '2026-04-09', 'debit',  0, 'Metrô',                       NULL),
(4, 2, 4, 3,    150.00, '2026-04-10', 'debit',  0, 'Cinema e popcorn',            NULL),
(4, 5, 4, NULL,1200.00, '2026-04-10', 'credit', 0, 'Dividendos FII',              NULL),
(4, 1, 3, NULL, 250.00, '2026-04-12', 'debit',  0, 'Consulta médica',             NULL),
(4, 2, 1, 1,    280.60, '2026-04-13', 'debit',  0, 'Mercado quinzenal',           NULL),
(4, 4, 2, 2,    165.00, '2026-04-14', 'debit',  0, 'Gasolina',                    NULL),
(4, 1, 5, NULL, 800.00, '2026-04-15', 'debit',  0, 'Condomínio',                  NULL),
(4, 3, 4, 3,    380.00, '2026-04-16', 'debit',  4, 'Headphone parcelado',         NULL),
(4, 4, 1, NULL, 180.00, '2026-04-17', 'credit', 0, 'Reembolso alimentação',       NULL),
(4, 1, 1, 5,     88.90, '2026-04-18', 'debit',  0, 'Almoço de negócios',          NULL),
(4, 2, 3, 4,     55.00, '2026-04-19', 'debit',  0, 'Exame de sangue',             NULL),
(4, 4, 5, NULL, 350.00, '2026-04-20', 'debit',  0, 'IPTU parcela',                NULL),
(4, 1, 2, NULL,  42.00, '2026-04-21', 'debit',  0, 'Uber',                        NULL),
(4, 2, 1, 1,    198.75, '2026-04-22', 'debit',  0, 'Mercado',                     NULL),
(4, 1, 4, NULL, 500.00, '2026-04-23', 'credit', 0, 'Presente recebido',           NULL),
(4, 3, 5, NULL, 800.00, '2026-04-24', 'debit',  0, 'Condomínio Bradesco',         NULL),
(4, 4, 4, NULL,  29.90, '2026-04-25', 'debit',  0, 'Disney+',                     NULL),
(4, 1, 1, 5,     95.20, '2026-04-26', 'debit',  0, 'Lanche',                      NULL),
(4, 2, 2, NULL,  60.00, '2026-04-27', 'debit',  0, 'Passagem aérea parcial',      NULL),
(4, 4, 3, NULL, 150.00, '2026-04-28', 'debit',  0, 'Plano de saúde',              NULL),
(4, 3, 4, NULL, 300.00, '2026-04-29', 'credit', 0, 'Rendimento poupança',         NULL),
-- ▸ MAIO
(4, 4, 5, NULL, 5200.00,'2026-05-01', 'credit', 0, 'Salário maio BB',             NULL),
(4, 4, 5, NULL, 2200.00,'2026-05-01', 'debit',  0, 'Aluguel maio',                NULL),
(4, 1, 1, 1,    230.15, '2026-05-02', 'debit',  0, 'Mercado',                     NULL),
(4, 2, 2, 2,    110.00, '2026-05-02', 'debit',  0, 'Combustível',                 NULL),
(4, 2, 5, NULL, 4500.00,'2026-05-03', 'credit', 0, 'Salário maio Itaú',           NULL),
(4, 2, 5, NULL, 1800.00,'2026-05-03', 'debit',  0, 'Aluguel Itaú maio',           NULL),
(4, 3, 3, 4,     78.50, '2026-05-03', 'debit',  0, 'Farmácia',                    NULL),
(4, 1, 4, NULL,  45.00, '2026-05-04', 'debit',  0, 'Streaming',                   NULL),
(4, 4, 2, NULL,  35.00, '2026-05-04', 'debit',  0, 'Uber',                        NULL),
(4, 2, 1, 1,    265.30, '2026-05-05', 'debit',  0, 'Mercado',                     NULL),
(4, 1, 3, NULL, 180.00, '2026-05-05', 'debit',  0, 'Consulta odontológica',       NULL),
(4, 5, 4, NULL, 800.00, '2026-05-05', 'credit', 0, 'Dividendos FII maio',         NULL),
(4, 4, 5, NULL, 350.00, '2026-05-06', 'debit',  0, 'IPTU parcela',                NULL),
(4, 1, 1, 5,     66.80, '2026-05-06', 'debit',  0, 'Almoço',                      NULL),
(4, 2, 2, NULL,  25.00, '2026-05-06', 'debit',  0, 'Estacionamento',              NULL);
