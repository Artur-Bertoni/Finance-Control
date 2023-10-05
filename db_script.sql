CREATE TABLE IF NOT EXISTS artur_user
(
    id       INT AUTO_INCREMENT,
    name     VARCHAR(75) NOT NULL,
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
    FOREIGN KEY (user_id) REFERENCES artur_user (id)
);


CREATE TABLE IF NOT EXISTS artur_account
(
    id                       INT AUTO_INCREMENT,
    user_id                  INT         NOT NULL,
    financial_institution_id INT         NOT NULL,
    name                     VARCHAR(50) NOT NULL,
    contect                  VARCHAR(100),
    description              VARCHAR(150),

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES artur_user (id),
    FOREIGN KEY (financial_institution_id) REFERENCES artur_financial_institution (id)
);


CREATE TABLE IF NOT EXISTS artur_transaction_locale
(
    id      INT AUTO_INCREMENT,
    user_id INT         NOT NULL,
    name    VARCHAR(50) NOT NULL,
    address VARCHAR(150),

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES artur_user (id)
);


CREATE TABLE IF NOT EXISTS artur_category
(
    id          INT AUTO_INCREMENT,
    user_id     INT         NOT NULL,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(250),

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES artur_user (id)
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
    plots_number          INT,
    obs                   VARCHAR(255),
    is_installments       BOOLEAN DEFAULT FALSE,
    last_charge           DATE,

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES artur_user (id),
    FOREIGN KEY (account_id) REFERENCES artur_account (id),
    FOREIGN KEY (category_id) REFERENCES artur_category (id),
    FOREIGN KEY (transaction_locale_id) REFERENCES artur_transaction_locale (id)
);
