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
    echo '<script>alert("Usuário e senha incorretos")</script>';
    header("Location: ../Login.html");
}

if (array_key_exists('registerButton', $_POST)) {
    header("Location: ../User.html");
}
