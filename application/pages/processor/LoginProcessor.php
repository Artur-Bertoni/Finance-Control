<?php
$fixedUsername = "user";
$fixedPassword = "123";

$userUsername = $_POST["usernameField"];
$userPassword = $_POST["passwordField"];

if (($userUsername == $fixedUsername) && ($userPassword == $fixedPassword)) {
    session_start();

    $_SESSION["username"] = $userUsername;
    $_SESSION["password"] = $userPassword;

    header("Location: ../HomePage.html");
}
else {
    echo "<script>alert('Usuário e senha incorretos');location.href=\"../Login.html\";</script>";
}

if (array_key_exists('registerButton', $_POST)) {
    session_start();

    header("Location: ../User.html");
}
