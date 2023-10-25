<?php

include "../../backend/services/UserService.php";

session_start();

if (isset($_POST['registerButton'])) {
    header("Location: ../User.html");
    exit;
}

$service = new UserService();

$email = $_POST["emailField"];
$password = $_POST["passwordField"];

$result = $service->login($email, $password);

if (!$result instanceof User) {
    echo "<script>alert('" . $result . "');location.href=\"../Login.html\";</script>";
    exit;
}

$_SESSION["userId"] = $result->getId();
header("Location: ../HomePage.html");

$email = $_POST["emailField"];
$password = $_POST["passwordField"];
