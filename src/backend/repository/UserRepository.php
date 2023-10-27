<?php

include_once "../../utils/DBConnection.php";
include_once "../../backend/entities/User.php";

class UserRepository
{
    public function save(UserRequestDTO $requestDTO): User|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("insert into artur_user
            (username, email, password) values(?, ?, ?)");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $username = $requestDTO->getUsername();
            $email = $requestDTO->getEmail();
            $password = $requestDTO->getPassword();

            $stmt->bind_param("sss",
                $username, $email, $password
            );

            if ($stmt->execute()) {
                $lastInsertedId = $stmt->insert_id;
                $stmt->close();

                $user = $this->findById($lastInsertedId);

                $db->commit();
                return $user;
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function update($id, UserRequestDTO $requestDTO): User|string
    {
        try {
            global $db;
            $db->begin_transaction();

            $stmt = $db->prepare("update artur_user set
            username = ?, email = ?, password = ? where id = ?");

            if (!$stmt)
                throw new SQLiteException("Prepare failed: (" . $db->errno . ") " . $db->error);

            $username = $requestDTO->getUsername();
            $email = $requestDTO->getEmail();
            $password = $requestDTO->getPassword();

            $stmt->bind_param("sssi",
                $username, $email, $password, $id
            );

            if ($stmt->execute()) {
                $stmt->close();

                $user = $this->findById($id);

                $db->commit();
                return $user;
            } else
                throw new SQLiteException("Execute failed: (" . $stmt->errno . ") " . $stmt->error);
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }

    }

    public function findById($id): User|string
    {
        try {
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

            return "Usuário de id " . $id . " não encontrado";
        } catch (Error|Throwable $e) {
            return "Erro: " . $e->getMessage();
        }
    }

    public function findByEmail($email): bool|string
    {
        try {
            global $db;

            return $db->query("SELECT * FROM artur_user WHERE email = '$email'");
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function findByEmailForUpdate($id, $email): bool|string
    {
        try {
            global $db;

            $result = $db->query("SELECT * FROM artur_user WHERE email = '$email' and id != $id");

            return $result->num_rows > 0;
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function findByEmailAndPassword($email, $password): User|string
    {
        try {
            global $db;

            $result = $db->query("select * from artur_user where email = '$email' and password like '$password'");

            if ($result->num_rows > 0) {
                $user = $result->fetch_assoc();
                return new User($user['id'], $user['username'], $user['email'], $user['password']);
            }

            return "Usuário de email " . $email . " e senha " . $password . " não encontrado";
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }

    public function delete($id): ?string
    {
        try {
            global $db;

            $db->begin_transaction();
            $db->query("delete from artur_user where id = $id");
            $db->commit();

            return null;
        } catch (Error|Throwable $e) {
            $db->rollback();
            return "Erro: " . $e->getMessage();
        }
    }
}
