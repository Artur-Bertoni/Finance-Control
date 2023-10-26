<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/User.php";

class UserRepository
{
    public function save(UserRequestDTO $requestDTO)
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

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

    public function findByEmailAndPassword($email, $password)
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

            $result = $db->query("select * from artur_user where email = '" . $email . "' and password like '" . $password . "'");

            if ($result->num_rows > 0) {
                $user = $result->fetch_assoc();
                return new User($user['id'], $user['username'], $user['email'], $user['password']);
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

    public function update($id, UserRequestDTO $requestDTO)
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

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

    public function findById($id): User|bool
    {
        try {
            openConnection();
            global $db;
            $db->begin_transaction();

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

            $db->query("delete from artur_user where id = $id");
        } catch (Exception $e) {
            global $db;
            $db->rollback();
            die("Erro: " . $e);
        } finally {
            closeConnection();
        }
    }
}
