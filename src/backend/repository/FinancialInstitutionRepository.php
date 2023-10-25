<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/FinancialInstitution.php";

class FinancialInstitutionRepository
{
    public function save(FinancialInstitutionRequestDTO $requestDTO)
    {
        global $db;

        $stmt = $db->prepare("insert into artur_financial_institution
        (user_id, name, address, contact) values(?, ?, ?, ?)");

        if (!$stmt)
            die("Prepare failed: (" . $db->errno . ") " . $db->error);

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

            return findById($lastInsertedId);
        } else
            die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
    }

    public function update($id, FinancialInstitutionRequestDTO $requestDTO)
    {
        global $db;

        $stmt = $db->prepare("update artur_financial_institution set
        user_id = ?, name = ?, address = ?, contact = ? where id = ?");

        if (!$stmt)
            die("Prepare failed: (" . $db->errno . ") " . $db->error);

        $userId = $requestDTO->getUserId();
        $name = $requestDTO->getName();
        $address = $requestDTO->getAddress();
        $contact = $requestDTO->getContact();

        $stmt->bind_param("isssi",
            $userId, $name, $address, $contact, $id
        );

        if ($stmt->execute()) {
            $stmt->close();

            return $this->findById($id);
        } else
            die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
    }

    public function findById($id)
    {
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
        return false;
    }

    public function findAllByUserId($userId): array
    {
        global $db;

        $result = $db->query("select * from artur_financial_institution where user_id = $userId");

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
    }

    public function delete($id)
    {
        global $db;

        $db->query("delete from artur_financial_institution where id = $id");
    }
}
