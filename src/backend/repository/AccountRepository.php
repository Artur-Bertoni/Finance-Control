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
            (user_id, financial_institution_id, name, contact, description) values(?, ?, ?, ?, ?)");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

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
            user_id = ?, financial_institution_id = ?, name = ?, contact = ?, description = ? where id = ?");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

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
                    $account['description']
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
                    $row['description']
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

            $db->begin_transaction();
            $db->query("delete from artur_account where id = $id");
            $db->commit();

            return null;
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }
}
