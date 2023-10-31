<?php

include_once "../../utils/DBConnection.php";
include_once "../../backend/entities/Category.php";

class CategoryRepository
{
    public function save(CategoryRequestDTO $requestDTO): Category|bool|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("insert into artur_category
            (user_id, name, description) values(?, ?, ?)");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $name = $requestDTO->getName();
            $description = $requestDTO->getDescription();

            $stmt->bind_param("iss",
                $userId, $name, $description
            );

            if ($stmt->execute()) {
                $lastInsertedId = $stmt->insert_id;
                $stmt->close();

                $category = $this->findById($lastInsertedId);

                $db->commit();
                return $category;
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function update($id, CategoryRequestDTO $requestDTO): Category|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("update artur_category set
            user_id = ?, name = ?, description = ? where id = ?");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $userId = $requestDTO->getUserId();
            $name = $requestDTO->getName();
            $description = $requestDTO->getDescription();

            $stmt->bind_param("issi",
                $userId, $name, $description, $id
            );

            if ($stmt->execute()) {
                $stmt->close();

                $category = $this->findById($id);

                $db->commit();
                return $category;
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function findById($id): Category|string
    {
        try {
            global $db;

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
            return "Categoria de id " . $id . " não encontrada";
        } catch (Error|Throwable $e) {
            return "Erro: " . $e->getMessage();
        }
    }

    public function findAllByUserId($userId): array|string
    {
        try {
            global $db;

            $result = $db->query("select * from artur_category where user_id = $userId order by id desc");

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
        } catch (Error|Throwable $e) {
            return "Erro: " . $e->getMessage();
        }
    }

    public function delete($id): ?string
    {
        try {
            global $db;

            $db->begin_transaction();
            $db->query("delete from artur_category where id = $id");
            $db->commit();

            return null;
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }
}
