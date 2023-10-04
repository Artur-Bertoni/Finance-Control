<?php

if ($_POST["passwordField"] == $_POST["passwordConfirmationField"]) {
    $_SESSION["username"] = $_POST["usernameField"];
    $_SESSION["password"] = $_POST["passwordField"];

    header("Location: ../HomePage.html");
}
else {
    echo "<script>alert('As senhas devem ser iguais');location.href=\"../User.html\";</script>";
}

if (array_key_exists('cancelButton', $_POST)) {
    header("Location: ../Login.html");
}
