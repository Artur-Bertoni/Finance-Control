<?php

include "../../backend/services/UserService.php";

session_start();

$service = new UserService();

if (isset($_POST['homeButton']) || isset($_POST['cancelButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

$userId = $_SESSION['userId'];
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
