<?php

include_once "../../utils/DBConnection.php";
include_once "../../backend/entities/Account.php";

class AccountRepository
{
    public function save(AccountRequestDTO $requestDTO): Account|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("insert into artur_account
            (user_id, financial_institution_id, name, contact, description, balance) values(?, ?, ?, ?, ?, ?)");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $financialInstitutionId = $requestDTO->getFinancialInstitutionId();
            $name = $requestDTO->getName();
            $contact = $requestDTO->getContact();
            $description = $requestDTO->getDescription();
            $balance = $requestDTO->getBalance();

            $stmt->bind_param("iisssd",
                $userId, $financialInstitutionId, $name, $contact, $description, $balance
            );

            if ($stmt->execute()) {
                $lastInsertedId = $stmt->insert_id;
                $stmt->close();

                $account = $this->findById($lastInsertedId);

                $db->commit();
                return $account;
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function update($id, AccountRequestDTO $requestDTO): Account|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("update artur_account set
            user_id = ?, financial_institution_id = ?, name = ?, contact = ?, description = ?, balance = ? where id = ?");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $financialInstitutionId = $requestDTO->getFinancialInstitutionId();
            $name = $requestDTO->getName();
            $contact = $requestDTO->getContact();
            $description = $requestDTO->getDescription();
            $balance = $requestDTO->getBalance();

            $stmt->bind_param("iisssdi",
                $userId, $financialInstitutionId, $name, $contact, $description, $balance, $id
            );

            if ($stmt->execute()) {
                $stmt->close();

                $account = $this->findById($id);

                $db->commit();
                return $account;
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function findById($id): Account|string
    {
        try {
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
                    $account['description'],
                    $account['balance']
                );
            }
            return "Conta de id " . $id . " não encontrada";
        } catch (Error|Throwable $e) {
            return "Erro: " . $e->getMessage();
        }
    }

    public function findAllByUserId($userId): array|string
    {
        try {
            global $db;

            $result = $db->query("select * from artur_account where user_id = $userId order by id desc");

            $accounts = array();
            while ($row = mysqli_fetch_array($result)) {
                $accounts[] = new Account(
                    $row['id'],
                    $row['user_id'],
                    $row['financial_institution_id'],
                    $row['name'],
                    $row['contact'],
                    $row['description'],
                    $row['balance']
                );
            }
            return $accounts;
        } catch (Error|Throwable $e) {
            return "Erro: " . $e->getMessage();
        }
    }

    public function delete($id): ?string
    {
        try {
            global $db;

            $db->begin_transaction();
            $db->query("delete from artur_account where id = $id");
            $db->commit();

            return null;
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function totalAccountsValue($accountId)
    {
        try {
            global $db;

            $query = "select Sum(balance) as total_value from artur_account ";

            if ($accountId !== '')
                $query = $query . "where id = $accountId";

            $result = $db->query($query);

            if ($result->num_rows > 0)
                return $result->fetch_assoc()['total_value'];

            throw new SQLiteException("Erro ao retornar o valor total de conta");
        } catch (Error|Throwable $e) {
            return "Erro: " . $e->getMessage();
        }
    }

    public function patchBalance($id, $value)
    {
        try {
            global $db;

            $db->begin_transaction();
            $stmt = $db->prepare("update artur_account set balance = balance + ? where id = ?");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $stmt->bind_param("di", $value, $id);

            if ($stmt->execute()) {
                $stmt->close();
                $db->commit();
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }
}
