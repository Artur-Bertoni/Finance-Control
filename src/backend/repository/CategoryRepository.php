<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/Category.php";

class CategoryRepository
{
    public function save(CategoryRequestDTO $requestDTO) {
        global $db;

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

            return findById($lastInsertedId);
        } else
            die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
    }

    public function update($id, CategoryRequestDTO $requestDTO) {
        global $db;

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
    }

    public function findById($id) {
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
        return false;
    }

    public function findAllByUserId($userId): array {
        global $db;

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
    }

    public function delete($id) {
        global $db;

        $db->query("delete from artur_category where id = $id");
    }
}
