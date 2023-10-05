<?php

include "../../backend/services/UserService.php";

if (isset($_POST['registerButton'])) {
    header("Location: ../User.html");
    exit;
}

$service = new UserService();

$email = $_POST["emailField"];
$password = $_POST["passwordField"];

if ($user = $service->login($email, $password)) {
    $_SESSION["username"] = $user->getUsername();
    $_SESSION["email"] = $email;
    $_SESSION["password"] = $password;
    header("Location: ../HomePage.html");
}

$email = $_POST["emailField"];
$password = $_POST["passwordField"];
