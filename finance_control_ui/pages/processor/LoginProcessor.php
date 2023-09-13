<?php
$fixedUsername = "user";
$fixedPassword = "123";

$userUsername = $_POST["usernameField"];
$userPassword = $_POST["passwordField"];

if (($userUsername == $fixedUsername) && ($userPassword == $fixedPassword)) {
    echo "Login efetuado com sucesso";
    header("Location: ../HomePage.html");
}
else {
    echo "Usuário e/ou senha incorreto";
}


