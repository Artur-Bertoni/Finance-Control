<?php

include "../../backend/services/AccountService.php";

session_start();

$service = new AccountService();

if (array_key_exists('postAccount', $_POST)) {
    $_SESSION['accountId'] = "";
    header("Location: ../Account.html");
    exit;
}

if (array_key_exists('itemButton', $_POST)) {
    $_SESSION['accountId'] = $_POST['itemButton'];
    header("Location: ../Account.html");
    exit;
}

if (isset($_POST['homeButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}
