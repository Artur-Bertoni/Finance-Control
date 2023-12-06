<?php

include "../../backend/services/UserService.php";

session_start();

$service = new UserService();

if (isset($_POST['homeButton']) || (isset($_POST['cancelButton']) && $_SESSION['userId'] != "")) {
    header("Location: ../HomePage.html");
    exit;
} elseif (isset($_POST['cancelButton']) && $_SESSION['userId'] == "") {
    header("Location: ../Login.html");
    exit;
}

if (isset($_POST['deleteButton'])) {
    $result = $service->delete($_SESSION['userId']);

    if ($result != null) {
        echo "<script>alert('" . $result . "');location.href=\"../User.html\";</script>";
        exit;
    }

    $_SESSION['userId'] = "";
    header("Location: ../Login.html");
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
exit;
