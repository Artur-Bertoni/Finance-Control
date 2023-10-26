<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/TransactionLocale.php";

class TransactionLocaleRepository
{
    public function save(TransactionLocaleRequestDTO $requestDTO)
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("insert into artur_transaction_locale
            (user_id, name, address) values(?, ?, ?)");

            if (!$stmt)
                die("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $name = $requestDTO->getName();
            $address = $requestDTO->getAddress();

            $stmt->bind_param("iss",
                $userId, $name, $address
            );

            if ($stmt->execute()) {
                $lastInsertedId = $stmt->insert_id;
                $stmt->close();

                return $this->findById($lastInsertedId);
            } else
                die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Exception $e) {
            global $db;
            $db->rollback();
            die("Erro: " . $e);
        } finally {
            closeConnection();
        }
    }

    public function update($id, TransactionLocaleRequestDTO $requestDTO)
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("update artur_transaction_locale set
            user_id = ?, name = ?, address = ? where id = ?");

            if (!$stmt)
                die("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $name = $requestDTO->getName();
            $address = $requestDTO->getAddress();

            $stmt->bind_param("issi",
                $userId, $name, $address, $id
            );

            if ($stmt->execute()) {
                $stmt->close();

                return $this->findById($id);
            } else
                die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Exception $e) {
            global $db;
            $db->rollback();
            die("Erro: " . $e);
        } finally {
            closeConnection();
        }
    }

    public function findById($id): TransactionLocale|false
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

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
            return false;
        } catch (Exception $e) {
            global $db;
            $db->rollback();
            die("Erro: " . $e);
        } finally {
            closeConnection();
        }
    }

    public function findAllByUserId($userId): array
    {
        try {
            openConnection();
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
        } catch (Exception $e) {
            global $db;
            $db->rollback();
            die("Erro: " . $e);
        } finally {
            closeConnection();
        }
    }

    public function delete($id): void
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

            $db->query("delete from artur_transaction_locale where id = $id");
        } catch (Exception $e) {
            global $db;
            $db->rollback();
            die("Erro: " . $e);
        } finally {
            closeConnection();
        }
    }
}
