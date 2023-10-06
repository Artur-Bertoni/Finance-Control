<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/User.php";

function save(UserRequestDTO $userRequestDTO) {
    global $db;

    $db->query("insert into artur_user(username, email, password) values(
                                                         '" . $userRequestDTO->getUsername() . "',
                                                         '" . $userRequestDTO->getEmail() . "',
                                                         '" . $userRequestDTO->getPassword() . "')");
    $user = $db->query("select * from artur_user where id = last_insert_id()")->fetch_assoc();
    return new User($user['id'], $user['username'], $user['email'], $user['password']);
}

function findByEmail($email) {
    global $db;
    $result = $db->query("select * from artur_user where email = '" . $email . "'");

    if ($result->num_rows == 0) {
        $user = $result->fetch_assoc();
        return new User($user['id'], $user['username'], $user['email'], $user['password']);
    }
    return false;
}

function findByEmailAndPassword($email, $password) {
    global $db;
    $result = $db->query("select * from artur_user where email = '" . $email . "' and password like '" . $password . "'");

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();
        return new User($user['id'], $user['username'], $user['email'], $user['password']);
    }
    return false;
}

function update($id, UserRequestDTO $userRequestDTO) {
    global $db;

    $db->query("update artur_user set
                      username = '" . $userRequestDTO->getUsername() . "',
                      email = '" . $userRequestDTO->getEmail() . "',
                      password = '" . $userRequestDTO->getPassword() . "'
                      where id = " . $id);
    $user = $db->query("select * from artur_user where id = " . $id)->fetch_assoc();
    return new User($user['id'], $user['username'], $user['email'], $user['password']);
}


