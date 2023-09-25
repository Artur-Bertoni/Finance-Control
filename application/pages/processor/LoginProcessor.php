<?php
$fixedUsername = "user";
$fixedPassword = "123";

$userUsername = $_POST["usernameField"];
$userPassword = $_POST["passwordField"];

if (($userUsername == $fixedUsername) && ($userPassword == $fixedPassword)) {
    header("Location: ../HomePage.html");
}
else {
    echo '<script>alert("I am an alert box!")</script>';

}

if (array_key_exists('registerButton', $_POST)) {
    header("Location: ../User.html");
}