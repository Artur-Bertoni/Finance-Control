<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/Transaction.php";

function save(TransactionRequestDTO $transactionRequestDTO) {
    global $db;

    $stmt = $db->prepare("insert into artur_transaction
    (user_id, account_id, category_id, transaction_locale_id, value, date, type, installments_number, obs)
    values(?, ?, ?, ?, ?, ?, ?, ?, ?)");

    if (!$stmt)
        die("Prepare failed: (" . $db->errno . ") " . $db->error);

    $userId = $transactionRequestDTO->getUserId();
    $accountId = $transactionRequestDTO->getAccountId();
    $categoryId = $transactionRequestDTO->getCategoryId();
    $transactionLocaleId = $transactionRequestDTO->getTransactionLocaleId();
    $value = $transactionRequestDTO->getValue();
    $date = $transactionRequestDTO->getDate();
    $type = $transactionRequestDTO->getType();
    $installmentsNumber = $transactionRequestDTO->getInstallmentsNumber();
    $obs = $transactionRequestDTO->getObs();

    $stmt->bind_param("iiiidssis",
        $userId,
        $accountId,
        $categoryId,
        $transactionLocaleId,
        $value,
        $date,
        $type,
        $installmentsNumber,
        $obs
    );

    if ($stmt->execute()) {
        $lastInsertedId = $stmt->insert_id;
        $stmt->close();

        return $this->findById($lastInsertedId);
    } else
        die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
}

function update($id, TransactionRequestDTO $transactionRequestDTO) {
    global $db;

    $stmt = $db->prepare("update artur_transaction set
    user_id = ?, account_id = ?, category_id = ?, transaction_locale_id = ?, value = ?, date = ?, type = ?, installments_number = ?, obs = ?
    where id = ?");

    if (!$stmt)
        die("Prepare failed: (" . $db->errno . ") " . $db->error);

    $userId = $transactionRequestDTO->getUserId();
    $accountId = $transactionRequestDTO->getAccountId();
    $categoryId = $transactionRequestDTO->getCategoryId();
    $transactionLocaleId = $transactionRequestDTO->getTransactionLocaleId();
    $value = $transactionRequestDTO->getValue();
    $date = $transactionRequestDTO->getDate();
    $type = $transactionRequestDTO->getType();
    $installmentsNumber = $transactionRequestDTO->getInstallmentsNumber();
    $obs = $transactionRequestDTO->getObs();

    $stmt->bind_param("iiiidssisi",
        $userId,
        $accountId,
        $categoryId,
        $transactionLocaleId,
        $value,
        $date,
        $type,
        $installmentsNumber,
        $obs,
        $id
    );

    if ($stmt->execute()) {
        $stmt->close();

        return $this->findById($id);
    } else
        die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
}

function findById($id) {
    global $db;

    $transaction = $db->query("SELECT * FROM artur_transaction WHERE id = $id")->fetch_assoc();
    return new Transaction(
        $transaction['id'],
        $transaction['userId'],
        $transaction['accountId'],
        $transaction['categoryId'],
        $transaction['transactionLocaleId'],
        $transaction['value'],
        $transaction['date'],
        $transaction['type'],
        $transaction['installmentsNumber'],
        $transaction['obs']
    );
}
