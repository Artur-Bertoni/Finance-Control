<?php

include_once "../../utils/DBConnection.php";
include_once "../../backend/entities/TransactionLocale.php";

class TransactionLocaleRepository
{
    public function save(TransactionLocaleRequestDTO $requestDTO): TransactionLocale|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("insert into artur_transaction_locale
            (user_id, name, address) values(?, ?, ?)");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $name = $requestDTO->getName();
            $address = $requestDTO->getAddress();

            $stmt->bind_param("iss",
                $userId,
                $name,
                $address
            );

            if ($stmt->execute()) {
                $lastInsertedId = $stmt->insert_id;
                $stmt->close();

                $transactionLocale = $this->findById($lastInsertedId);

                $db->commit();
                return $transactionLocale;
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function update($id, TransactionLocaleRequestDTO $requestDTO): TransactionLocale|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("update artur_transaction_locale set
            user_id = ?, name = ?, address = ? where id = ?");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $name = $requestDTO->getName();
            $address = $requestDTO->getAddress();

            $stmt->bind_param("issi",
                $userId,
                $name,
                $address,
                $id
            );

            if ($stmt->execute()) {
                $stmt->close();

                $transactionLocale = $this->findById($id);

                $db->commit();
                return $transactionLocale;
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function findById($id): TransactionLocale|false
    {
        try {
            global $db;

            $result = $db->query("SELECT * FROM artur_transaction_locale WHERE id = $id");

            if ($result->num_rows > 0) {
                $transactionLocale = $result->fetch_assoc();
                return new TransactionLocale(
                    $transactionLocale['id'],
                    $transactionLocale['user_id'],
                    $transactionLocale['name'],
                    $transactionLocale['address']
                );
            }

            return "Local de Transação de id " . $id . " não encontrada";
        } catch (Error|Throwable $e) {
            return "Erro: " . $e->getMessage();
        }
    }

    public function findAllByUserId($userId): array|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $result = $db->query("select * from artur_transaction_locale where user_id = $userId");

            $transactionLocales = array();
            while ($row = mysqli_fetch_array($result)) {
                $transactionLocales[] = new TransactionLocale(
                    $row['id'],
                    $row['user_id'],
                    $row['name'],
                    $row['address']
                );
            }
            return $transactionLocales;
        } catch (Error|Throwable $e) {
            return "Erro: " . $e->getMessage();
        }
    }

    public function delete($id): ?string
    {
        try {
            global $db;
            $db->begin_transaction();

            $db->begin_transaction();
            $db->query("delete from artur_transaction_locale where id = $id");
            $db->commit();

            return null;
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }
}
