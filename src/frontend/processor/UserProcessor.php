<?php

include "../../backend/services/UserService.php";

if (isset($_POST['cancelButton'])) {
    header("Location: ../Login.html");
    exit;
}

$service = new UserService();

$username = $_POST["usernameField"];
$email = $_POST["emailField"];
$password = $_POST["passwordField"];
$passwordConfirmation = $_POST["passwordConfirmationField"];

$service->create(new UserRequestDTO($username, $email, $password, $passwordConfirmation));

$_SESSION["username"] = $_POST["usernameField"];
$_SESSION["email"] = $_POST["emailField"];
$_SESSION["password"] = $_POST["passwordField"];

header("Location: ../HomePage.html");
