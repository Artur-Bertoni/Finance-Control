<?php

include "../../backend/services/TransactionLocaleService.php";

session_start();

$service = new TransactionLocaleService();

if (array_key_exists('postTransactionLocale', $_POST)) {
    $_SESSION['transactionLocaleId'] = "";
    header("Location: ../TransactionLocale.html");
    exit;
}

if (array_key_exists('itemButton', $_POST)) {
    $_SESSION['transactionLocaleId'] = $_POST['itemButton'];
    header("Location: ../TransactionLocale.html");
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
