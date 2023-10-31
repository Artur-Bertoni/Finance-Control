<?php

include_once "../../utils/DBConnection.php";
include_once "../../backend/entities/FinancialInstitution.php";

class FinancialInstitutionRepository
{
    public function save(FinancialInstitutionRequestDTO $requestDTO): FinancialInstitution|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("insert into artur_financial_institution
            (user_id, name, address, contact) values(?, ?, ?, ?)");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $name = $requestDTO->getName();
            $address = $requestDTO->getAddress();
            $contact = $requestDTO->getContact();

            $stmt->bind_param("isss",
                $userId, $name, $address, $contact
            );

            if ($stmt->execute()) {
                $lastInsertedId = $stmt->insert_id;
                $stmt->close();

                $financialInstitution = $this->findById($lastInsertedId);

                $db->commit();
                return $financialInstitution;
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function update($id, FinancialInstitutionRequestDTO $requestDTO): FinancialInstitution|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("update artur_financial_institution set
            user_id = ?, name = ?, address = ?, contact = ? where id = ?");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $name = $requestDTO->getName();
            $address = $requestDTO->getAddress();
            $contact = $requestDTO->getContact();

            $stmt->bind_param("isssi",
                $userId, $name, $address, $contact, $id
            );

            if ($stmt->execute()) {
                $stmt->close();

                $financialInstitution = $this->findById($id);

                $db->commit();
                return $financialInstitution;
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function findById($id): FinancialInstitution|false
    {
        try {
            global $db;

            $result = $db->query("SELECT * FROM artur_financial_institution WHERE id = $id");

            if ($result->num_rows > 0) {
                $financialInstitution = $result->fetch_assoc();
                return new FinancialInstitution(
                    $financialInstitution['id'],
                    $financialInstitution['user_id'],
                    $financialInstitution['name'],
                    $financialInstitution['address'],
                    $financialInstitution['contact']
                );
            }
            return "Instituição Financeira de id " . $id . " não encontrada";
        } catch (Error|Throwable $e) {
            return "Erro: " . $e->getMessage();
        }
    }

    public function findAllByUserId($userId): array|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $result = $db->query("select * from artur_financial_institution where user_id = $userId order by id desc");

            $financialInstitution = array();
            while ($row = mysqli_fetch_array($result)) {
                $financialInstitution[] = new FinancialInstitution(
                    $row['id'],
                    $row['user_id'],
                    $row['name'],
                    $row['address'],
                    $row['contact']
                );
            }
            return $financialInstitution;
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
            $db->query("delete from artur_financial_institution where id = $id");
            $db->commit();

            return null;
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }
}
