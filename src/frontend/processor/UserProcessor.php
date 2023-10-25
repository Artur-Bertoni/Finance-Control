<?php

include "../../backend/services/UserService.php";

session_start();

if (isset($_POST['cancelButton'])) {
    header("Location: ../Login.html");
    exit;
}

$service = new UserService();

$userId = $_POST['userId'];
$username = $_POST["usernameField"];
$email = $_POST["emailField"];
$password = $_POST["passwordField"];
$passwordConfirmation = $_POST["passwordConfirmationField"];

if ($userId != "") {
    $result = $service->update($userId, new UserRequestDTO(
        $username,
        $email,
        $password,
        $passwordConfirmation
    ));
} else {
    $result = $service->create(new UserRequestDTO(
        $username,
        $email,
        $password,
        $passwordConfirmation
    ));
}

if (!$result instanceof User) {
    echo "<script>alert('" . $result . "');location.href=\"../User.html\";</script>";
    exit;
}

$_SESSION["userId"] = $result->getId();

header("Location: ../HomePage.html");
