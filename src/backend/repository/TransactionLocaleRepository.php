<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/TransactionLocale.php";

function save(TransactionLocaleRequestDTO $transactionLocaleRequestDTO) {
    global $db;

    $stmt = $db->prepare("insert into artur_transaction_locale
    (user_id, name, address) values(?, ?, ?)");

    if (!$stmt)
        die("Prepare failed: (" . $db->errno . ") " . $db->error);

    $userId = $transactionLocaleRequestDTO->getUserId();
    $name = $transactionLocaleRequestDTO->getName();
    $address = $transactionLocaleRequestDTO->getAddress();

    $stmt->bind_param("iss",
        $userId, $name, $address
    );

    if ($stmt->execute()) {
        $lastInsertedId = $stmt->insert_id;
        $stmt->close();

        return findById($lastInsertedId);
    } else
        die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
}

function update($id, TransactionLocaleRequestDTO $transactionLocaleRequestDTO) {
    global $db;

    $stmt = $db->prepare("update artur_transaction_locale set
    user_id = ?, name = ?, address = ? where id = ?");

    if (!$stmt)
        die("Prepare failed: (" . $db->errno . ") " . $db->error);

    $userId = $transactionLocaleRequestDTO->getUserId();
    $name = $transactionLocaleRequestDTO->getName();
    $address = $transactionLocaleRequestDTO->getAddress();

    $stmt->bind_param("issi",
        $userId, $name, $address, $id
    );

    if ($stmt->execute()) {
        $stmt->close();

        return $this->findById($id);
    } else
        die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
}

function findById($id) {
    global $db;

    $result = $db->query("SELECT * FROM artur_transaction_locale WHERE id = $id");

    if ($result->num_rows > 0) {
        $transactionLocale = $result->fetch_assoc();
        return new TransactionLocale(
            $transactionLocale['id'],
            $transactionLocale['userId'],
            $transactionLocale['name'],
            $transactionLocale['address']
        );
    }
    return false;
}

function delete($id) {
    global $db;

    $db->query("delete from artur_transaction_locale where id = $id");
}
