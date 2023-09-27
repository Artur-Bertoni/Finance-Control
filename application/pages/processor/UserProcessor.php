<?php

header("Location: ../HomePage.html");

setcookie('username', $userUsername);
setcookie('password', $userPassword);

if (array_key_exists('cancelButton', $_POST)) {
    header("Location: ../Login.html");
}
