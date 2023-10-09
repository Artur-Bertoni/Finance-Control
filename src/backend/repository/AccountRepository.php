<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/Account.php";

function save(AccountRequestDTO $requestDTO) {
    global $db;

    $stmt = $db->prepare("insert into artur_account
    (user_id, financial_institution_id, name, contact, description) values(?, ?, ?, ?, ?)");

    if (!$stmt)
        die("Prepare failed: (" . $db->errno . ") " . $db->error);

    $userId = $requestDTO->getUserId();
    $financialInstitutionId = $requestDTO->getFinancialInstitutionId();
    $name = $requestDTO->getName();
    $contact = $requestDTO->getContact();
    $description = $requestDTO->getDescription();

    $stmt->bind_param("iisss",
        $userId, $financialInstitutionId, $name, $contact, $description
    );

    if ($stmt->execute()) {
        $lastInsertedId = $stmt->insert_id;
        $stmt->close();

        return findById($lastInsertedId);
    } else
        die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
}

function update($id, AccountRequestDTO $requestDTO) {
    global $db;

    $stmt = $db->prepare("update artur_account set
    user_id = ?, financial_institution_id = ?, name = ?, contact = ?, description = ? where id = ?");

    if (!$stmt)
        die("Prepare failed: (" . $db->errno . ") " . $db->error);

    $userId = $requestDTO->getUserId();
    $financialInstitutionId = $requestDTO->getFinancialInstitutionId();
    $name = $requestDTO->getName();
    $contact = $requestDTO->getContact();
    $description = $requestDTO->getDescription();

    $stmt->bind_param("iisssi",
        $userId, $financialInstitutionId, $name, $contact, $description, $id
    );

    if ($stmt->execute()) {
        $stmt->close();

        return $this->findById($id);
    } else
        die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
}

function findById($id) {
    global $db;

    $result = $db->query("SELECT * FROM artur_account WHERE id = $id");

    if ($result->num_rows > 0) {
        $account = $result->fetch_assoc();
        return new Account(
            $account['id'],
            $account['user_id'],
            $account['financial_institution_id'],
            $account['name'],
            $account['contact'],
            $account['description']
        );
    }
    return false;
}

function delete($id) {
    global $db;

    $db->query("delete from artur_financial_institution where id = $id");
}
