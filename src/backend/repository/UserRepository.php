<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/User.php";

class UserRepository
{
    public function save(UserRequestDTO $requestDTO)
    {
        global $db;

        $stmt = $db->prepare("insert into artur_user
        (username, email, password) values(?, ?, ?)");

        if (!$stmt)
            die("Prepare failed: (" . $db->errno . ") " . $db->error);

        $username = $requestDTO->getUsername();
        $email = $requestDTO->getEmail();
        $password = $requestDTO->getPassword();

        $stmt->bind_param("sss",
            $username, $email, $password
        );

        if ($stmt->execute()) {
            $lastInsertedId = $stmt->insert_id;
            $stmt->close();

            return findById($lastInsertedId);
        } else
            die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
    }

    public function findByEmail($email)
    {
        global $db;
        $result = $db->query("select * from artur_user where email = '" . $email . "'");

        if ($result->num_rows == 0) {
            $user = $result->fetch_assoc();
            return new User($user['id'], $user['username'], $user['email'], $user['password']);
        }
        return false;
    }

    public function findByEmailAndPassword($email, $password)
    {
        global $db;
        $result = $db->query("select * from artur_user where email = '" . $email . "' and password like '" . $password . "'");

        if ($result->num_rows > 0) {
            $user = $result->fetch_assoc();
            return new User($user['id'], $user['username'], $user['email'], $user['password']);
        }
        return false;
    }

    public function update($id, UserRequestDTO $requestDTO)
    {
        global $db;

        $stmt = $db->prepare("update artur_user set
        username = ?, email = ?, password = ? where id = ?");

        if (!$stmt)
            die("Prepare failed: (" . $db->errno . ") " . $db->error);

        $username = $requestDTO->getUsername();
        $email = $requestDTO->getEmail();
        $password = $requestDTO->getPassword();

        $stmt->bind_param("sssi",
            $username, $email, $password, $id
        );

        if ($stmt->execute()) {
            $lastInsertedId = $stmt->insert_id;
            $stmt->close();

            return findById($lastInsertedId);
        } else
            die("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
    }

    public function findById($id)
    {
        global $db;

        $result = $db->query("SELECT * FROM artur_user WHERE id = $id");

        if ($result->num_rows > 0) {
            $user = $result->fetch_assoc();
            return new User(
                $user['id'],
                $user['username'],
                $user['email'],
                $user['password']
            );
        }
        return false;
    }

    public function delete($id)
    {
        global $db;

        $db->query("delete from artur_user where id = $id");
    }
}
