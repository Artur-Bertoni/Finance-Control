<?php

include_once "../../utils/db_connection.php";
include_once "../../backend/entities/User.php";

function save(UserRequestDTO $userRequestDTO) {
    global $db;

    $db->query("insert into artur_user(username, email, password) values(
                                                         '" . $userRequestDTO->getUsername() . "',
                                                         '" . $userRequestDTO->getEmail() . "',
                                                         '" . $userRequestDTO->getPassword() . "')");
    $user = $db->query("select * from artur_user where email = '" . $userRequestDTO->getEmail() . "'")->fetch_assoc();
    return new User($user['id'], $user['username'], $user['email'], $user['password']);
}

function findByEmail($email) {
    global $db;

    return $db->query("select * from artur_user where email = '" . $email . "'");
}

function findByEmailAndPassword($email, $password) {
    global $db;

    return $db->query("select * from artur_user where email = '" . $email . "' and password like '".$password."'");
}
