<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/Category.php";

class CategoryRepository
{
    public function save(CategoryRequestDTO $requestDTO)
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("insert into artur_category
            (user_id, name, description) values(?, ?, ?)");

            if (!$stmt)
                die("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $name = $requestDTO->getName();
            $description = $requestDTO->getDescription();

            $stmt->bind_param("iss",
                $userId, $name, $description
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

    public function update($id, CategoryRequestDTO $requestDTO)
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("update artur_category set
            user_id = ?, name = ?, description = ? where id = ?");

            if (!$stmt)
                die("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $name = $requestDTO->getName();
            $description = $requestDTO->getDescription();

            $stmt->bind_param("issi",
                $userId, $name, $description, $id
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

    public function findById($id): bool|Category
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

            $result = $db->query("SELECT * FROM artur_category WHERE id = $id");

            if ($result->num_rows > 0) {
                $account = $result->fetch_assoc();
                return new Category(
                    $account['id'],
                    $account['user_id'],
                    $account['name'],
                    $account['description']
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

            $result = $db->query("select * from artur_category where user_id = $userId");

            $categories = array();
            while ($row = mysqli_fetch_array($result)) {
                $categories[] = new Category(
                    $row['id'],
                    $row['user_id'],
                    $row['name'],
                    $row['description']
                );
            }
            return $categories;
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

            $db->query("delete from artur_category where id = $id");
        } catch (Exception $e) {
            global $db;
            $db->rollback();
            die("Erro: " . $e);
        } finally {
            closeConnection();
        }
    }
}
