<?php

include "../../backend/services/UserService.php";

session_start();

$service = new UserService();

if (isset($_POST['registerButton'])) {
    $_SESSION['userId'] = "";
    header("Location: ../User.html");
    exit;
}

if (isset($_POST['loginButton'])) {
    $email = $_POST["emailField"];
    $password = $_POST["passwordField"];

    $result = $service->login($email, $password);

    if (!$result instanceof User) {
        echo "<script>alert('" . $result . "');location.href=\"../Login.html\";</script>";
        exit;
    }

    $_SESSION["userId"] = $result->getId();
    header("Location: ../HomePage.html");
    exit;
}

