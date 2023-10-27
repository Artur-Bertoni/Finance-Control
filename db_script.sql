CREATE TABLE IF NOT EXISTS artur_user
(
    id       INT AUTO_INCREMENT,
    username VARCHAR(75) NOT NULL,
    email    VARCHAR(50) NOT NULL,
    password VARCHAR(75) NOT NULL,

    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS artur_financial_institution
(
    id      INT AUTO_INCREMENT,
    user_id INT         NOT NULL,
    name    VARCHAR(50) NOT NULL,
    address VARCHAR(150),
    contact VARCHAR(100),

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES artur_user (id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS artur_account
(
    id                       INT AUTO_INCREMENT,
    user_id                  INT         NOT NULL,
    financial_institution_id INT         NOT NULL,
    name                     VARCHAR(50) NOT NULL,
    contact                  VARCHAR(100),
    description              TINYTEXT,

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES artur_user (id) ON DELETE CASCADE,
    FOREIGN KEY (financial_institution_id) REFERENCES artur_financial_institution (id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS artur_transaction_locale
(
    id      INT AUTO_INCREMENT,
    user_id INT         NOT NULL,
    name    VARCHAR(50) NOT NULL,
    address VARCHAR(150),

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES artur_user (id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS artur_category
(
    id          INT AUTO_INCREMENT,
    user_id     INT         NOT NULL,
    name        VARCHAR(50) NOT NULL,
    description TINYTEXT,

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES artur_user (id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS artur_transaction
(
    id                    INT AUTO_INCREMENT,
    user_id               INT         NOT NULL,
    account_id            INT         NOT NULL,
    category_id           INT         NOT NULL,
    transaction_locale_id INT,
    value                 DOUBLE      NOT NULL,
    date                  DATE        NOT NULL,
    type                  VARCHAR(10) NOT NULL,
    installments_number   INT,
    obs                   TINYTEXT,
    is_installments       BOOLEAN DEFAULT FALSE,
    last_charge           DATE,

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES artur_user (id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES artur_account (id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES artur_category (id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_locale_id) REFERENCES artur_transaction_locale (id) ON DELETE CASCADE
);
