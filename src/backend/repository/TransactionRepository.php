<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/Transaction.php";

class TransactionRepository {
    public function save(TransactionRequestDTO $requestDTO) {
        global $db;

        $stmt = $db->prepare("insert into artur_transaction
        (user_id, account_id, category_id, transaction_locale_id, value, date, type, installments_number, obs)
        values(?, ?, ?, ?, ?, ?, ?, ?, ?)");

        if (!$stmt)
            die("Prepare failed: (" . $db->errno . ") " . $db->error);

        $userId = $requestDTO->getUserId();
        $accountId = $requestDTO->getAccountId();
        $categoryId = $requestDTO->getCategoryId();
        $transactionLocaleId = $requestDTO->getTransactionLocaleId();
        $value = $requestDTO->getValue();
        $date = $requestDTO->getDate();
        $type = $requestDTO->getType();
        $installmentsNumber = $requestDTO->getInstallmentsNumber();
        $obs = $requestDTO->getObs();

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

            return findById($lastInsertedId);
        } else
            die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
    }

    public function update($id, TransactionRequestDTO $requestDTO) {
        global $db;

        $stmt = $db->prepare("update artur_transaction set
        user_id = ?, account_id = ?, category_id = ?, transaction_locale_id = ?, value = ?, date = ?, type = ?, installments_number = ?, obs = ?
        where id = ?");

        if (!$stmt)
            die("Prepare failed: (" . $db->errno . ") " . $db->error);

        $userId = $requestDTO->getUserId();
        $accountId = $requestDTO->getAccountId();
        $categoryId = $requestDTO->getCategoryId();
        $transactionLocaleId = $requestDTO->getTransactionLocaleId();
        $value = $requestDTO->getValue();
        $date = $requestDTO->getDate();
        $type = $requestDTO->getType();
        $installmentsNumber = $requestDTO->getInstallmentsNumber();
        $obs = $requestDTO->getObs();

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

    public function findById($id) {
        global $db;

        $result = $db->query("SELECT * FROM artur_transaction WHERE id = $id");

        if ($result->num_rows > 0) {
            $transaction = $result->fetch_assoc();
            return new Transaction(
                $transaction['id'],
                $transaction['user_id'],
                $transaction['account_id'],
                $transaction['category_id'],
                $transaction['transaction_locale_id'],
                $transaction['value'],
                $transaction['date'],
                $transaction['type'],
                $transaction['installments_number'],
                $transaction['obs']
            );
        }
        return false;
    }

    public function delete($id) {
        global $db;

        $db->query("delete from artur_transaction where id = $id");
    }

    public function findAllByUserId($userId): array {
        global $db;

        $result = $db->query("select * from artur_transaction where user_id = $userId");

        $transactions = array();
        while ($row = mysqli_fetch_array($result)) {
            $transactions[] = new Transaction(
                $row['id'],
                $row['user_id'],
                $row['account_id'],
                $row['category_id'],
                $row['transaction_locale_id'],
                $row['value'],
                $row['date'],
                $row['type'],
                $row['installments_number'],
                $row['obs']
            );
        }
        return $transactions;
    }
}
